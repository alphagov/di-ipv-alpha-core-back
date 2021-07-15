package uk.gov.di.ipv.core.back.domain.data;

import lombok.Builder;
import lombok.Data;
import uk.gov.di.ipv.core.back.domain.gpg45.EvidenceScore;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;

import java.util.UUID;

@Data
@Builder
public class IdentityEvidence {
//    private UUID uuid;
    private EvidenceType type;
    private Object evidenceData;
    private ValidityCheck validityChecks;
    private EvidenceScore evidenceScore;

    public static IdentityEvidence fromDto(EvidenceDto evidenceDto) {
        return IdentityEvidence.builder()
//            .uuid(evidenceDto.getEvidenceId())
            .type(evidenceDto.getType())
            .evidenceData(evidenceDto.getEvidenceData())
            .build();
    }
}
