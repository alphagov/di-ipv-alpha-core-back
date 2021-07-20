package uk.gov.di.ipv.core.back.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.UserInfoDto;

import java.text.ParseException;

public interface OAuth2Service {

    AuthorizationResponse doAuthorize(SessionData sessionData);

    TokenResponse exchangeCodeForToken(TokenRequest tokenRequest) throws JOSEException;

    UserInfoDto handleUserInfo(AccessToken accessToken) throws ParseException;

    JWKSet getJwks();
}
