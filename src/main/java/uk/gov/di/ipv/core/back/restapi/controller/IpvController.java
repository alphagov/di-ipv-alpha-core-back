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
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
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
    // IPV front -> ATP front -> IPV front -> IPV back -> ATP back -> IPV back -> GPG45 -> IPV back -> IPV front/ORC

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
        var identityEvidence = IdentityEvidence.fromDto(evidenceDto);
        sessionData.getIdentityVerificationBundle()
            .getIdentityEvidence()
            .add(identityEvidence);
        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        var calculateResponseDtoMono = gpg45Service.calculate(verificationBundle);

        var sessionDataDto = calculateResponseDtoMono.flatMap(gpg45Response -> {
            var bundle = gpg45Response.getIdentityVerificationBundle();
            var profile = gpg45Response.getMatchedIdentityProfile();

            sessionData.setIdentityProfile(profile);
            sessionData.setIdentityVerificationBundle(bundle);
            sessionService.saveSession(sessionData);

            return Mono.just(SessionDataDto.fromSessionData(sessionData));
        });

        return sessionDataDto.map(ResponseEntity::ok);
    }
}
