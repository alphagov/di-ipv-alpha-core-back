package uk.gov.di.ipv.core.back.service;

import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;

public interface EvidenceService {

    Mono<EvidenceDto> addEvidence(SessionData sessionData, EvidenceDto evidenceDto);
}
