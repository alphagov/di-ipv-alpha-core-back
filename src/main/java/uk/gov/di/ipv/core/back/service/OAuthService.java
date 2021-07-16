package uk.gov.di.ipv.core.back.service;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import uk.gov.di.ipv.core.back.domain.SessionData;

import java.util.UUID;

public interface OAuthService {
    AuthorizationResponse doAuthorize(SessionData sessionData);

    TokenResponse exchangeCodeForToken(TokenRequest tokenRequest);

    void handleUserInfo();
}
