package uk.gov.di.ipv.core.back.service;

import uk.gov.di.ipv.core.back.domain.SessionData;

import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    UUID createSession();

    Optional<SessionData> getSession(UUID sessionId);

    UUID saveSession(SessionData sessionData);
}
