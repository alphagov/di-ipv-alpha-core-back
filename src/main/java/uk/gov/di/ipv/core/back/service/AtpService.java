package uk.gov.di.ipv.core.back.service;

import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;

public interface AtpService {

    Mono<IdentityEvidence> processEvidence(EvidenceDto evidenceDto);
}
