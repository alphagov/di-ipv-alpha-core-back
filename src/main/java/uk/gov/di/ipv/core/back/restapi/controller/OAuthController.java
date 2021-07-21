package uk.gov.di.ipv.core.back.restapi.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.restapi.dto.TokenRequestDto;
import uk.gov.di.ipv.core.back.restapi.dto.UserInfoDto;
import uk.gov.di.ipv.core.back.service.OAuth2Service;

import java.util.Map;

@Slf4j
@RestController
public class OAuthController {

    private final OAuth2Service oAuth2Service;

    @Autowired
    public OAuthController(OAuth2Service oauth2Service) {
        this.oAuth2Service = oauth2Service;
    }

    @PostMapping(
        value = "/oauth2/token",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    public Mono<ResponseEntity<JSONObject>> exchangeCodeForToken(
        final TokenRequestDto dto
    ) throws JOSEException {
        var tokenRequest = new TokenRequest(
            null,
            new ClientID(dto.getClient_id()),
            new AuthorizationCodeGrant(
                new AuthorizationCode(dto.getCode()),
                dto.getRedirect_uri())
        );

        var tokenResponse = oAuth2Service.exchangeCodeForToken(tokenRequest);

        if (tokenResponse instanceof TokenErrorResponse) {
            var errorResponse = (TokenErrorResponse) tokenResponse;

            return Mono.just(errorResponse.toJSONObject())
                .map(ResponseEntity::ok);
        }

        var accessToken = (AccessTokenResponse) tokenResponse;
        return Mono.just(accessToken.toJSONObject())
            .map(ResponseEntity::ok);
    }

    @GetMapping("/.well-known/jwks.json")
    public Mono<ResponseEntity<Map<String, Object>>> getJwks() {
        var jwks = oAuth2Service.getJwks();
        return Mono.just(jwks.toJSONObject())
            .map(ResponseEntity::ok);
    }

    @GetMapping(
        value = "/oauth2/userinfo",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public Mono<ResponseEntity<UserInfoDto>> userInfo(
        @RequestHeader("Authorization") String accessTokenString
    ) throws Exception {
        var accessToken = AccessToken.parse(accessTokenString);
        var userInfoResponse = oAuth2Service.handleUserInfo(accessToken);

        return Mono.just(userInfoResponse)
            .map(ResponseEntity::ok);
    }
}
