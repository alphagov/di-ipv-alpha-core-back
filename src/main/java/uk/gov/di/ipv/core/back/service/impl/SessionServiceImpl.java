package uk.gov.di.ipv.core.back.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import uk.gov.di.ipv.core.back.domain.AuthData;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.service.SessionService;
import uk.gov.di.ipv.core.back.util.ClaimsUtil;

import java.util.ArrayList;
import java.util.HashMap;
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
    public UUID createSession(AuthorizationRequest authorizationRequest) {
        var sessionId = UUID.randomUUID();

        log.info("Creating a new session with session id: {}", sessionId);

        var sessionData = createNewSessionData(authorizationRequest);
        sessionData.setSessionId(sessionId);

        var serialized = serializeSessionData(sessionData);

        redisClient.set(sessionId.toString(), serialized);
        return sessionId;
    }

    private SessionData createNewSessionData(AuthorizationRequest authorizationRequest) {
        var sessionData = new SessionData();
        var bundle = new IdentityVerificationBundle();
        bundle.setIdentityEvidence(new ArrayList<>());
        sessionData.setIdentityVerificationBundle(bundle);

        var authData = new AuthData();
        authData.setClientID(authorizationRequest.getClientID());
        authData.setRedirectURI(authorizationRequest.getRedirectionURI());
        authData.setScope(authorizationRequest.getScope());
        authData.setState(authorizationRequest.getState());
        authData.setResponseMode(authorizationRequest.getResponseMode());
        authData.setRequestedAttributes(authorizationRequest.getCustomParameter("claims"));
        sessionData.setAuthData(authData);
        sessionData.setCollectedAttributes(new HashMap<>());

        try {
            var requestedLevelOfConfidence = ClaimsUtil.getRequestedLevelOfConfidence(sessionData);
            sessionData.setRequestedLevelOfConfidence(requestedLevelOfConfidence);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }

        return sessionData;
    }

    @Override
    public Optional<SessionData> getSession(UUID sessionId) {
        var serialized = redisClient.get(sessionId.toString());

        if (serialized == null || serialized.equals("nil")) {
            log.warn("Could not find session data in redis cache");
            return Optional.empty();
        }

        var deserialized = deserializeSessionData(serialized);
        log.info("Retrieved a session {} from cache", sessionId);

        return Optional.of(deserialized);
    }

    @Override
    public UUID saveSession(SessionData sessionData) {
        var sessionId = sessionData.getSessionId();

        if (!redisClient.exists(sessionId.toString())) {
            log.warn("Specified key does not exist, {}", sessionId);
        }

        var serialized = serializeSessionData(sessionData);
        redisClient.set(sessionId.toString(), serialized);

        log.info("Saved session {} to cache", sessionData.getSessionId());

        return sessionId;
    }

    @Override
    public void saveAuthCode(AuthorizationCode code, UUID sessionId) {
        if (!redisClient.exists(code.getValue())) {
            log.warn("Key already exists");
        }

        redisClient.set(code.getValue(), sessionId.toString());
    }

    @Override
    public UUID getSessionIdFromCode(String authorizationCode) {
        return UUID.fromString(redisClient.get(authorizationCode));
    }

    @Override
    public void saveAccessToken(String accessToken, UUID sessionID) {
        redisClient.set(accessToken, sessionID.toString());
    }

    @Override
    public SessionData getSessionDataFromAccessToken(String accessToken) {
        return deserializeSessionData(redisClient.get(accessToken));
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
