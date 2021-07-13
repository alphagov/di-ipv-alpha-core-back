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
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.EvidenceService;
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
    private final EvidenceService evidenceService;

    @Autowired
    public IpvController(
        Gpg45Service gpg45Service,
        SessionService sessionService,
        RoutingService routingService,
        EvidenceService evidenceService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
        this.routingService = routingService;
        this.evidenceService = evidenceService;
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
    public Mono<ResponseEntity<SessionDataDto>> addEvidence(@PathVariable("session-id") UUID sessionId, @RequestBody EvidenceDto evidenceDto) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        // TODO: create ATP service
        //  when identity evidence is submitted,
        //  send the evidence data object to whatever atp
        //  create the ATP response into identity evidence object
        //  send identity verification bundle to GPG45
        //  save session data

        var sessionData = maybeSessionData.get();

        // TODO: Extract these into static functions?
        //  would neaten things up by calling `.flatMap(SomeService::addEvidenceToBundle)`
        var sessionDataDto = evidenceService.processEvidence(evidenceDto)
            .flatMap(identityEvidence -> {
                sessionData.getIdentityVerificationBundle()
                    .getIdentityEvidence()
                    .add(identityEvidence);
                return Mono.just(sessionData);
            })
            .flatMap(session -> {
                var verificationBundle = new VerificationBundleDto(session.getIdentityVerificationBundle());
                return gpg45Service.calculate(verificationBundle);
            })
            .flatMap(calculateResponseDto -> {
               var bundle = calculateResponseDto.getIdentityVerificationBundle();
               var profile = calculateResponseDto.getMatchedIdentityProfile();

               sessionData.setIdentityProfile(profile);
               sessionData.setIdentityVerificationBundle(bundle);
               sessionService.saveSession(sessionData);

               return Mono.just(SessionDataDto.fromSessionData(sessionData));
            });

        return sessionDataDto.map(ResponseEntity::ok);
    }
}
