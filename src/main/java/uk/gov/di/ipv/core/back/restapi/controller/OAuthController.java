package uk.gov.di.ipv.core.back.restapi.controller;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.restapi.dto.TokenRequestDto;
import uk.gov.di.ipv.core.back.service.OAuthService;

@Slf4j
@RestController
@RequestMapping(value = "/oauth2")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(
        OAuthService oauthService
    ) {
        this.oAuthService = oauthService;
    }

    @PostMapping(
        value = "/token",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        }
    )
    public Mono<ResponseEntity<TokenResponse>> exchangeCodeForToken(
        TokenRequestDto dto
    ) {
        var tokenRequest = new TokenRequest(
            null,
            new ClientID(dto.getClient_id()),
            new AuthorizationCodeGrant(
                new AuthorizationCode(dto.getCode()),
                dto.getRedirect_uri())
        );

        var tokenResponse = oAuthService.exchangeCodeForToken(tokenRequest);

        return Mono.just(tokenResponse)
            .map(ResponseEntity::ok);
    }
}
