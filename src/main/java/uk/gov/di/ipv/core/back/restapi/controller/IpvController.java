package uk.gov.di.ipv.core.back.restapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.RoutingService;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.UUID;

@RestController
@RequestMapping("/ipv")
public class IpvController {

    private final Gpg45Service gpg45Service;
    private final SessionService sessionService;
    private final RoutingService routingService;

    @Autowired
    public IpvController(
        Gpg45Service gpg45Service,
        SessionService sessionService,
        RoutingService routingService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
        this.routingService = routingService;
    }

    // ORC -> IPV Front
    // IPV Front -> IPV back start a new session
    // IPV Back - UUID > IPV front
    // IPV Front -> requests next route from IPV back
    // IPV back -> checks session data and returns next route it should do

    // When adding evidence from ATP:
    // IPV front -> ATP front -> IPV front -> IPV back -> ATP back

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
    public void addEvidence(@PathVariable("session-id") UUID sessionId, IdentityEvidence identityEvidence) {
        // TODO: Add evidence to session.
        //  return 200 ok, or other statuses.
    }
}
