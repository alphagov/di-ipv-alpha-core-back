package uk.gov.di.ipv.core.back.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    private final Jedis redisClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SessionServiceImpl(
        @Qualifier("redis-client") Jedis redisClient,
        ObjectMapper objectMapper
    ) {
        this.redisClient = redisClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public UUID createSession() {
        var sessionId = UUID.randomUUID();
        var sessionData = new SessionData();
        var bundle = new IdentityVerificationBundle();

        // TODO: tidy this up
        bundle.setIdentityEvidence(new ArrayList<>());
        sessionData.setSessionId(sessionId);
        sessionData.setIdentityVerificationBundle(bundle);
        var serialized = serializeSessionData(sessionData);

        redisClient.set(sessionId.toString(), serialized);
        return sessionId;
    }


    @Nullable
    @Override
    public Optional<SessionData> getSession(UUID sessionId) {
        var serialized = redisClient.get(sessionId.toString());

        if (serialized == null || serialized.equals("nil")) {
            log.warn("Could not find session data in redis cache");
            return Optional.empty();
        }

        var deserialized = deserializeSessionData(serialized);
        return Optional.of(deserialized);
    }

    @Override
    public UUID saveSession(SessionData sessionData) {
        var sessionId = sessionData.getSessionId();

        if (!redisClient.exists(sessionId.toString())) {
            log.warn("Specified key does not exist, {}", sessionId);
            // return sessionId; // TODO: do we need to return here with error?
        }

        var serialized = serializeSessionData(sessionData);
        redisClient.set(sessionId.toString(), serialized);

        return sessionId;
    }

    private String serializeSessionData(SessionData sessionData) {
        try {
            return objectMapper.writeValueAsString(sessionData);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize session data");
            throw new RuntimeException("Failed to serialize json data", exception);
        }
    }

    private SessionData deserializeSessionData(String serializedSessionData) {
        try {
            return objectMapper.readValue(serializedSessionData, SessionData.class);
        } catch (JsonProcessingException exception) {
            log.error("Failed to deserialize session data");
            throw new RuntimeException("Failed to deserialize json data", exception);
        }
    }
}
