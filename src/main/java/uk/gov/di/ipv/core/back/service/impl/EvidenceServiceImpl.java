package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.EvidenceType;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.AttributeCollectionService;
import uk.gov.di.ipv.core.back.service.EvidenceService;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class EvidenceServiceImpl implements EvidenceService {

    private final Gpg45Service gpg45Service;
    private final SessionService sessionService;
    private final AttributeCollectionService attributeCollectionService;

    public EvidenceServiceImpl(
        Gpg45Service gpg45Service,
        SessionService sessionService,
        AttributeCollectionService attributeCollectionService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
        this.attributeCollectionService = attributeCollectionService;
    }

    @Override
    public Mono<SessionDataDto> addEvidence(SessionData sessionData, EvidenceDto evidenceDto) {
        var identityEvidence = IdentityEvidence.fromDto(evidenceDto);

        log.info("Adding new evidence for session {}", sessionData.getSessionId());

        if (identityEvidence.getType().equals(EvidenceType.UK_PASSPORT)) {
            identityEvidence.getValidityChecks().setAuthoritativeSource("urn:di:ipv:atp-dcs");
        } else {
            identityEvidence.getValidityChecks().setAuthoritativeSource("");
        }

        sessionData.getIdentityVerificationBundle()
            .getIdentityEvidence()
            .add(identityEvidence);

        mockBundleScores(sessionData, evidenceDto);
        updateAttributesInSession(sessionData, identityEvidence);

        log.info(
            "Added new identity evidence {} for session {}",
            identityEvidence.getUuid(),
            sessionData.getSessionId());

        log.info("Posting identity verification bundle to GPG45 for session {}", sessionData.getSessionId());
        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        var calculateResponseDtoMono = gpg45Service.calculate(verificationBundle);

        return calculateResponseDtoMono.flatMap(gpg45Response -> {
            var bundle = gpg45Response.getIdentityVerificationBundle();
            var profile = gpg45Response.getMatchedIdentityProfile();

            sessionData.setIdentityProfile(profile);
            sessionData.setIdentityVerificationBundle(bundle);
            sessionService.saveSession(sessionData);

            return Mono.just(SessionDataDto.fromSessionData(sessionData));
        });
    }

    private void updateAttributesInSession(SessionData sessionData, IdentityEvidence identityEvidence) {
        var collectedAttributes = attributeCollectionService.collectAttributesFromEvidence(identityEvidence);
        var currentAttributes = sessionData.getCollectedAttributes();
        collectedAttributes.forEach((collectedAttributeName, collectedValue) -> {
            var combined = currentAttributes.compute(collectedAttributeName, (currentAttribute, currentValue) -> {
                // combine collected attribute values together
                if (currentValue == null) {
                    return collectedValue;
                }
                return Stream.concat(currentValue.stream(), collectedValue.stream()).collect(Collectors.toList());
            });

            currentAttributes.replace(collectedAttributeName, combined);
        });
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
