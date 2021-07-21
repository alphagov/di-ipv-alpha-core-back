package uk.gov.di.ipv.core.back.service;

import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.restapi.dto.SessionDataDto;

public interface EvidenceService {

    Mono<SessionDataDto> addEvidence(SessionData sessionData, EvidenceDto evidenceDto);
}
