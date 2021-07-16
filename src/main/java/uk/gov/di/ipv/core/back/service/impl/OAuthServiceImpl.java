package uk.gov.di.ipv.core.back.service.impl;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.service.OAuthService;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.UUID;

@Slf4j
@Service
public class OAuthServiceImpl implements OAuthService {

    private final SessionService sessionService;
    private final ClientID clientId;

    @Autowired
    public OAuthServiceImpl(
        @Qualifier("ipv-client-id") ClientID clientId,
        SessionService sessionService
    ) {
        this.clientId = clientId;
        this.sessionService = sessionService;
    }

    private boolean doesClientIdMatch(AuthorizationRequest authorizationRequest) {
        return clientId.equals(authorizationRequest.getClientID().getValue());
    }

    @Override
    public AuthorizationResponse doAuthorize(SessionData sessionData) {
        var callback = sessionData.getRedirectURI();
        var state = sessionData.getState();
        var responseMode = sessionData.getResponseMode();

//        if (!doesClientIdMatch(authorizationRequest)) {
//            log.warn("Authorization request client id does not match this client id");
//            return new AuthorizationErrorResponse(
//                callback,
//                OAuth2Error.ACCESS_DENIED,
//                state,
//                responseMode
//            );
//        }

        var code = new AuthorizationCode();
        sessionService.saveAuthCode(code, sessionData.getSessionId());

        return new AuthorizationSuccessResponse(
            callback,
            code,
            null,
            state,
            responseMode
        );

//        if (responseMode.equals(new ResponseType(ResponseType.Value.CODE))) {
//            var code = new AuthorizationCode();
//
//            sessionService.saveAuthCode(code, sessionData.getSessionId());
//
//            return new AuthorizationSuccessResponse(
//                callback,
//                code,
//                null,
//                state,
//                responseMode
//            );
//        } else if (responseMode.equals(new ResponseType(ResponseType.Value.TOKEN))) {
//            // may not need this
//            var accessToken = new BearerAccessToken();
//            // Set the access token -> session id
//
//            return new AuthorizationSuccessResponse(
//                callback,
//                null,
//                accessToken,
//                state,
//                responseMode
//            );
//        }

//        log.error("Unsupported authorization response type provided in authorization request");
//        return new AuthorizationErrorResponse(
//            callback,
//            OAuth2Error.ACCESS_DENIED,
//            state,
//            responseMode
//        );
    }

    @Override
    public TokenResponse exchangeCodeForToken(TokenRequest tokenRequest) {
//        if (!tokenRequest.getClientID().equals(clientId)) {
//            log.warn("Authorization request client id does not match this client id");
//            return new TokenErrorResponse(
//                new ErrorObject(
//                    OAuth2Error.ACCESS_DENIED_CODE,
//                    "Client id does not match")
//            );
//        }

        var params = tokenRequest.getAuthorizationGrant().toParameters();
        var code = params.get("code");
        var sessionId = sessionService.getSessionIdFromCode(code.stream().findFirst().get());

        if (tokenRequest.getAuthorizationGrant().getType().equals(GrantType.AUTHORIZATION_CODE)) {

            var accessToken = new BearerAccessToken();
            sessionService.saveAccessToken(accessToken.getValue(), sessionId);
            return new AccessTokenResponse(new Tokens(accessToken, null));
        }

        return new TokenErrorResponse(
            new ErrorObject("F-001", "Something failed during exchange of code to token")
        );
    }

    @Override
    public void handleUserInfo() {

    }
}
