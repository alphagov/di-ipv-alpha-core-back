package uk.gov.di.ipv.core.back.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.UserInfoDto;
import uk.gov.di.ipv.core.back.service.AttributeService;
import uk.gov.di.ipv.core.back.service.OAuth2Service;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class OAuth2ServiceImpl implements OAuth2Service {

    private final Key signingKey;
    private final String signingCertThumbprint;
    private final SessionService sessionService;
    private final AttributeService attributeService;
    private final Certificate signingCert;
    private final ClientID clientId;

    private final String issuerUrn = "urn:di:ipv:ipv-core";
    private final String orchestratorUrn = "urn:di:ipv:orchestrator";

    @Autowired
    public OAuth2ServiceImpl(
        @Qualifier("ipv-client-id") ClientID clientId,
        @Qualifier("ipv-signing-key") Key signingKey,
        @Qualifier("ipv-signing-cert-thumbprint") String signingCertThumbprint,
        @Qualifier("ipv-signing-cert") Certificate signingCert,
        SessionService sessionService,
        AttributeService attributeService
    ) {
        this.clientId = clientId;
        this.signingKey = signingKey;
        this.sessionService = sessionService;
        this.signingCertThumbprint = signingCertThumbprint;
        this.signingCert = signingCert;
        this.attributeService = attributeService;
    }

    @Override
    public AuthorizationResponse doAuthorize(SessionData sessionData) {
        var callback = sessionData.getAuthData().getRedirectURI();
        var state = sessionData.getAuthData().getState();
        var responseMode = sessionData.getAuthData().getResponseMode();
        var providedClientId = sessionData.getAuthData().getClientID().getValue();

        if (!doesClientIdMatch(providedClientId)) {
            log.warn("Authorization request client id does not match this client id");
            return new AuthorizationErrorResponse(
                callback,
                OAuth2Error.ACCESS_DENIED,
                state,
                responseMode
            );
        }

        var code = new AuthorizationCode();
        sessionService.saveAuthCode(code, sessionData.getSessionId());

        return new AuthorizationSuccessResponse(
            callback,
            code,
            null,
            state,
            responseMode
        );
    }

    @Override
    public TokenResponse exchangeCodeForToken(final TokenRequest tokenRequest) throws JOSEException {
        log.info("This client id: {}, provided client id: {}", clientId.toString(), tokenRequest.getClientID().toString());
        if (!tokenRequest.getClientID().toString().equals(clientId.toString())) {
            log.warn("Token request client id does not match this client id");
            return new TokenErrorResponse(
                new ErrorObject(
                    OAuth2Error.ACCESS_DENIED_CODE,
                    "Client id does not match")
            );
        }

        if (!tokenRequest.getAuthorizationGrant().getType().equals(GrantType.AUTHORIZATION_CODE)) {
            return new TokenErrorResponse(
                new ErrorObject("F-001", "Something failed during exchange of code to token")
            );
        }

        var params = tokenRequest.getAuthorizationGrant().toParameters();
        var code = params.get("code");

        if (code.isEmpty()) {
            return new TokenErrorResponse(
                new ErrorObject("F-002", "Authorization code does not exist")
            );
        }

        var sessionId = sessionService.getSessionIdFromCode(code.stream().findFirst().get());

        var payload = createJwsPayload();
        payload.appendField("sessionId", sessionId.toString());

        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .x509CertSHA256Thumbprint(new Base64URL(signingCertThumbprint))
            .build();

        var jwsObject = new JWSObject(
            header,
            new Payload(payload.toString())
        );

        jwsObject.sign(new RSASSASigner((PrivateKey) signingKey));
        var accessToken = new BearerAccessToken(jwsObject.serialize(), 3600, null);
        sessionService.saveAccessToken(accessToken.getValue(), sessionId);
        return new AccessTokenResponse(new Tokens(accessToken, null));
    }

    @Override
    public JWKSet getJwks() {
        var jwk = new RSAKey.Builder((RSAPublicKey) signingCert.getPublicKey())
            .algorithm(JWSAlgorithm.RS256)
            .x509CertSHA256Thumbprint(new Base64URL(signingCertThumbprint))
            .keyUse(KeyUse.SIGNATURE)
            .build();
        return new JWKSet(jwk);
    }

    @Override
    public UserInfoDto handleUserInfo(final AccessToken accessToken) throws ParseException {

        if (!isTokenValid(accessToken)) {
            throw new RuntimeException("Provided access token is not a valid token");
        }

        var accessTokenObj = accessToken.toJSONObject().get("access_token");
        var jwt = JWTParser.parse(accessTokenObj.toString());
        var sessionId = jwt.getJWTClaimsSet().getStringClaim("sessionId");
        var maybeSessionData = sessionService.getSession(UUID.fromString(sessionId));

        if (maybeSessionData.isEmpty()) {
            throw new RuntimeException("Empty session data");
        }

        var sessionData = maybeSessionData.get();
        return createUserInfoResponse(sessionData);
    }

    private UserInfoDto createUserInfoResponse(SessionData sessionData) {
        var userInfo = getDefaultUserInfo(sessionData);
        var aggregatedAttributes =
            attributeService.aggregateAttributes(sessionData);
        aggregatedAttributes.ifPresent(userInfo::putAll);

        return new UserInfoDto(userInfo);
    }

    private Map<String, Object> getDefaultUserInfo(SessionData sessionData) {
        var userInfo = new HashMap<String, Object>();

        userInfo.put("iss", issuerUrn);
        userInfo.put("aud", orchestratorUrn);
        userInfo.put("sub", orchestratorUrn);
        userInfo.put("identityProfile", sessionData.getIdentityProfile());
        userInfo.put("requestedLevelOfConfidence", sessionData.getRequestedLevelOfConfidence().toString());

        return userInfo;
    }

    private JSONObject createJwsPayload() {
        var json = new JSONObject();
        json.appendField("sub", "urn:di:ipv:orchestrator");
        json.appendField("iss", "urn:di:ipv:ipv-core");

        var instant = Instant.now();
        json.appendField("exp", instant.plus(3600, ChronoUnit.MINUTES).toEpochMilli());
        json.appendField("iat", instant.toEpochMilli());

        return json;
    }

    private boolean doesClientIdMatch(final String providedClientId) {
        return clientId.toString().equals(providedClientId);
    }

    private boolean isTokenValid(final AccessToken accessToken) {
        var keySource = new ImmutableJWKSet<>(getJwks());
        var expectedJwsAlgorithm = JWSAlgorithm.RS256;
        var keySelector = new JWSVerificationKeySelector<>(expectedJwsAlgorithm, keySource);
        var jwtProcessor = new DefaultJWTProcessor<>();

        jwtProcessor.setJWSTypeVerifier(
            new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("jwt"))
        );

        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
            new JWTClaimsSet.Builder().issuer("urn:di:ipv:ipv-core").build(),
            new HashSet<>(Arrays.asList("sub", "iat", "exp"))
        ));

        try {
            jwtProcessor.process(accessToken.toString(), null);
        } catch (Exception e) {
            log.error("Invalid JWT token received", e);
            return false;
        }

        return true;
    }
}
