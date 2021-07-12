package uk.gov.di.ipv.core.back.restapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.CalculateResponseDto;
import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.RoutingService;

import java.util.UUID;

@RestController
@RequestMapping("/ipv")
public class IpvController {

    private Gpg45Service gpg45Service;
    private RoutingService routingService;

    @Autowired
    public IpvController(
        Gpg45Service gpg45Service,
        RoutingService routingService
    ) {
        this.gpg45Service = gpg45Service;
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
    public Mono<ResponseEntity<UUID>> startNewSession() {
        return null;
    }

    @GetMapping("/get-route")
    public Mono<ResponseEntity<RouteDto>> getRoute(UUID sessionId) {
        return null;
    }

    @PostMapping("/add-evidence")
    public void addEvidence(UUID sessionId, IdentityEvidence identityEvidence) {
        // TODO: Add evidence to session.
        //  return 200 ok, or other statuses.
    }
}
