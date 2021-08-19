package uk.gov.di.ipv.core.back.restapi.controller;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;
import uk.gov.di.ipv.core.back.service.EvidenceService;
import uk.gov.di.ipv.core.back.service.OAuth2Service;
import uk.gov.di.ipv.core.back.service.RoutingService;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ipv")
public class IpvController {

    private final SessionService sessionService;
    private final RoutingService routingService;
    private final EvidenceService evidenceService;
    private final OAuth2Service oAuth2Service;

    @Autowired
    public IpvController(
        SessionService sessionService,
        RoutingService routingService,
        EvidenceService evidenceService,
        OAuth2Service oAuth2Service
    ) {
        this.sessionService = sessionService;
        this.routingService = routingService;
        this.evidenceService = evidenceService;
        this.oAuth2Service = oAuth2Service;
    }

    @GetMapping("/start-session")
    public Mono<ResponseEntity<SessionDataDto>> startNewSession(ServerHttpRequest request) throws ParseException {
        var authorizationRequest = AuthorizationRequest.parse(request.getURI().getQuery());
        var sessionId = sessionService.createSession(authorizationRequest);
        var dto = SessionDataDto.builder()
            .sessionId(sessionId)
            .build();

        return Mono.just(dto)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{session-id}/return")
    public Mono<ResponseEntity<AuthorizationResponse>> returnToOrchestrator(@PathVariable("session-id") UUID sessionId) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            log.warn("Session data not found for session id {}", sessionId);
            return Mono.just(ResponseEntity.notFound().build());
        }

        var sessionData = maybeSessionData.get();
        var authResponse = oAuth2Service.doAuthorize(sessionData);

        return Mono.just(authResponse)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{session-id}/get-route")
    public Mono<ResponseEntity<RouteDto>> getRoute(@PathVariable("session-id") UUID sessionId) {

        var route = routingService.getNextRoute(sessionId);
        return Mono.just(route)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{session-id}/session")
    public Mono<ResponseEntity<SessionDataDto>> getSessionData(@PathVariable("session-id") UUID sessionId) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            log.warn("Session data not found for session id {}", sessionId);
            return Mono.just(ResponseEntity.notFound().build());
        }

        var sessionData = maybeSessionData.get();
        var dto =  SessionDataDto.fromSessionData(sessionData);

        log.info("Found session data, returning session data for id {}", sessionId);

        return Mono.just(dto)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/{session-id}/add-evidence")
    public Mono<ResponseEntity<EvidenceDto>> addEvidence(@PathVariable("session-id") UUID sessionId, @RequestBody EvidenceDto evidenceDto) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        var sessionData = maybeSessionData.get();
        var evidenceDtoMono = evidenceService.addEvidence(sessionData, evidenceDto)
                .doOnSuccess(evidence ->
                    log.info("Evidence data added with the id {} for session {}",
                        evidence.getEvidenceId(),
                        sessionId));
        return evidenceDtoMono.map(ResponseEntity::ok);
    }

    @GetMapping("/{session-id}/evidence/{evidence-id}/delete")
    public Mono<ResponseEntity<Void>> deleteEvidence(@PathVariable("session-id") UUID sessionId, @PathVariable("evidence-id") UUID evidenceId) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            log.warn("Session data not found for session id {}", sessionId);
            return Mono.just(ResponseEntity.notFound().build());
        }

        var sessionData = maybeSessionData.get();
        var identityEvidence = sessionData
            .getIdentityVerificationBundle()
            .getIdentityEvidence()
            .stream()
            .filter(evidence -> evidence.getUuid().equals(evidenceId))
            .collect(Collectors.toList());

        if (identityEvidence.isEmpty()) {
            log.warn("Identity evidence not found for evidence id {}", evidenceId);
            return Mono.just(ResponseEntity.notFound().build());
        }

        log.info("Identity evidence {} was deleted from session {}", evidenceId, sessionId);

        var response = evidenceService.deleteEvidence(sessionData, identityEvidence.stream().findFirst().get());
        return response.map(ResponseEntity::ok);
    }
}
