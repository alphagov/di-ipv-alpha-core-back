package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.EvidenceType;

import java.util.UUID;

@Data
public class EvidenceDto {
    private UUID evidenceId;
    private EvidenceType type;
    private Object evidenceData;
}
