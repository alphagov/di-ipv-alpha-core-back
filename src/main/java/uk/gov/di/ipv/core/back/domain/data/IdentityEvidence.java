package uk.gov.di.ipv.core.back.domain.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.di.ipv.core.back.domain.gpg45.EvidenceScore;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityEvidence {
    private UUID uuid;
    private EvidenceType type;
    private Object evidenceData;
    private ValidityCheck validityChecks;
    private EvidenceScore evidenceScore;

    public static IdentityEvidence fromDto(EvidenceDto dto) {
        return new IdentityEvidence(
            UUID.randomUUID(),
            dto.getType(),
            dto.getEvidenceData(),
            new ValidityCheck(),
            null
        );
    }
}
