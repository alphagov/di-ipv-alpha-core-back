package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.EvidenceType;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.CalculateResponseDto;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.AttributeService;
import uk.gov.di.ipv.core.back.service.EvidenceService;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.SessionService;

@Slf4j
@Service
public class EvidenceServiceImpl implements EvidenceService {

    private final Gpg45Service gpg45Service;
    private final SessionService sessionService;
    private final AttributeService attributeService;

    public EvidenceServiceImpl(
        Gpg45Service gpg45Service,
        SessionService sessionService,
        AttributeService attributeService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
        this.attributeService = attributeService;
    }

    @Override
    public Mono<EvidenceDto> addEvidence(SessionData sessionData, EvidenceDto evidenceDto) {
        var identityEvidence = IdentityEvidence.fromDto(evidenceDto);

        log.info("Adding new evidence for session {}", sessionData.getSessionId());

        var authoritativeSource = getAuthoritativeSource(identityEvidence);
        identityEvidence.getValidityChecks().setAuthoritativeSource(authoritativeSource);

        sessionData.getIdentityVerificationBundle()
            .getIdentityEvidence()
            .add(identityEvidence);

        mockBundleScores(sessionData, evidenceDto);
        attributeService.updateAttributesInSession(sessionData, identityEvidence);

        log.info(
            "Added new identity evidence {} for session {}",
            identityEvidence.getUuid(),
            sessionData.getSessionId());

        log.info("Posting identity verification bundle to GPG45 for session {}", sessionData.getSessionId());
        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        var calculateResponseDtoMono = gpg45Service.calculate(verificationBundle);
        return calculateResponseDtoMono
            .flatMap(gpg45Response -> saveAndReturnSessionDto(gpg45Response, sessionData))
            .map(_sessionDataDto -> EvidenceDto.toDto(identityEvidence));
    }

    @Override
    public Mono<Void> deleteEvidence(SessionData sessionData, IdentityEvidence identityEvidence) {
        sessionData.getIdentityVerificationBundle().getIdentityEvidence().remove(identityEvidence);

        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        var calculateResponseDtoMono = gpg45Service.calculate(verificationBundle);
        return calculateResponseDtoMono
            .flatMap(gpg45Response -> saveAndReturnSessionDto(gpg45Response, sessionData))
            .then();
    }

    private Mono<SessionDataDto> saveAndReturnSessionDto(CalculateResponseDto gpg45Response, SessionData sessionData) {
        var bundle = gpg45Response.getIdentityVerificationBundle();
        var profile = gpg45Response.getMatchedIdentityProfile();

        sessionData.setIdentityProfile(profile);
        sessionData.setIdentityVerificationBundle(bundle);
        sessionService.saveSession(sessionData);

        return Mono.just(SessionDataDto.fromSessionData(sessionData));
    }

    private String getAuthoritativeSource(IdentityEvidence identityEvidence) {
        if (identityEvidence.getType() == EvidenceType.UK_PASSPORT) {
            return "urn:di:ipv:atp-dcs";
        }
        return "";
    }

    private void mockBundleScores(SessionData sessionData, EvidenceDto evidenceDto) {
        // mock the bundle scores for now
        sessionData.getIdentityVerificationBundle().getBundleScores()
            .setActivityCheckScore(evidenceDto.getBundleScores().getActivityCheckScore());
        sessionData.getIdentityVerificationBundle().getBundleScores()
            .setFraudCheckScore(evidenceDto.getBundleScores().getFraudCheckScore());
        sessionData.getIdentityVerificationBundle().getBundleScores()
            .setIdentityVerificationScore(evidenceDto.getBundleScores().getIdentityVerificationScore());
    }
}
