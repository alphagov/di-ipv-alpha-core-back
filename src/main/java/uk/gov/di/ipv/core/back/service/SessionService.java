package uk.gov.di.ipv.core.back.service;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import uk.gov.di.ipv.core.back.domain.SessionData;

import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    UUID createSession(AuthorizationRequest authorizationRequest);

    Optional<SessionData> getSession(UUID sessionId);

    UUID saveSession(SessionData sessionData);

    void saveAuthCode(AuthorizationCode code, UUID sessionId);

    UUID getSessionIdFromCode(String authorizationCode);

    void saveAccessToken(String accessToken, UUID sessionID);

    SessionData getSessionDataFromAccessToken(String accessToken);
}
