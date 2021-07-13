package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.service.AtpService;
import uk.gov.di.ipv.core.back.service.EvidenceService;

@Slf4j
@Service
public class EvidenceServiceImpl implements EvidenceService {

    private final AtpService passportAtpService;
    private final AtpService genericAtpService;

    public EvidenceServiceImpl(
        @Qualifier("passport-atp") AtpService passportAtpService,
        @Qualifier("generic-atp") AtpService genericAtpService
    ) {
        this.passportAtpService = passportAtpService;
        this.genericAtpService = genericAtpService;
    }

    @Override
    public Mono<IdentityEvidence> processEvidence(EvidenceDto evidenceDto) {
        switch (evidenceDto.getAttributeProvider()) {
            case PASSPORT:
                return passportAtpService.processEvidence(evidenceDto);
            case GENERIC:
                return genericAtpService.processEvidence(evidenceDto);
        }

        log.error("No matching attribute provider service available for type: {}", evidenceDto.getAttributeProvider());
        throw new RuntimeException("No matching attribute provider service available");
    }
}
