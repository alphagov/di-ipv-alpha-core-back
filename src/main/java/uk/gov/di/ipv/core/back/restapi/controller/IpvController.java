package uk.gov.di.ipv.core.back.restapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import uk.gov.di.ipv.core.back.service.RoutingService;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.UUID;

@RestController
@RequestMapping("/ipv")
public class IpvController {

    private final SessionService sessionService;
    private final RoutingService routingService;
    private final EvidenceService evidenceService;

    @Autowired
    public IpvController(
        SessionService sessionService,
        RoutingService routingService,
        EvidenceService evidenceService
    ) {
        this.sessionService = sessionService;
        this.routingService = routingService;
        this.evidenceService = evidenceService;
    }

    @GetMapping("/start-session")
    public Mono<ResponseEntity<SessionDataDto>> startNewSession() {
        var sessionId = sessionService.createSession();
        var dto = SessionDataDto.builder()
            .sessionId(sessionId)
            .build();

        return Mono.just(dto)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{session-id}/get-route")
    public Mono<ResponseEntity<RouteDto>> getRoute(@PathVariable("session-id") UUID sessionId) {

        var route = routingService.getNextRoute(sessionId);
        return Mono.just(route)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/{session-id}/add-evidence")
    public Mono<ResponseEntity<SessionDataDto>> addEvidence(@PathVariable("session-id") UUID sessionId, @RequestBody EvidenceDto evidenceDto) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        var sessionData = maybeSessionData.get();
        var sessionDataDto = evidenceService.addEvidence(sessionData, evidenceDto);

        return sessionDataDto.map(ResponseEntity::ok);
    }
}
