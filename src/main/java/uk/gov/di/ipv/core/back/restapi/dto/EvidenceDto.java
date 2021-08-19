package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.BundleScores;
import uk.gov.di.ipv.core.back.domain.data.EvidenceType;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.domain.gpg45.EvidenceScore;

import java.util.UUID;

@Data
public class EvidenceDto {
    private UUID evidenceId;
    private EvidenceType type;
    private Object evidenceData;

    // Only passing bundle scores here temporarily for mocking.
    private BundleScores bundleScores;
    private EvidenceScore evidenceScore;

    public static EvidenceDto toDto(IdentityEvidence identityEvidence) {
        var dto = new EvidenceDto();
        dto.setType(identityEvidence.getType());
        dto.setEvidenceId(identityEvidence.getUuid());
        dto.setEvidenceData(identityEvidence.getEvidenceData());
        dto.setEvidenceScore(identityEvidence.getEvidenceScore());

        return dto;
    }
}
