package uk.gov.di.ipv.core.back.service;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.CalculateResponseDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;

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

    Mono<SessionDataDto> saveAndReturnSessionDto(CalculateResponseDto gpg45Response, SessionData sessionData);
}
