package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.BundleScores;
import uk.gov.di.ipv.core.back.domain.data.EvidenceType;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;

import java.util.UUID;

@Data
public class EvidenceDto {
    private UUID evidenceId;
    private EvidenceType type;
    private Object evidenceData;
    private BundleScores bundleScores;

    public static EvidenceDto toDto(IdentityEvidence identityEvidence) {
        var dto = new EvidenceDto();
        dto.setType(identityEvidence.getType());
        dto.setEvidenceId(identityEvidence.getUuid());
        dto.setEvidenceData(identityEvidence.getEvidenceData());

        return dto;
    }
}
