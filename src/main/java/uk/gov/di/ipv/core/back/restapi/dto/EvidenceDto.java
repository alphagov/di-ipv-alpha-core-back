package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.AttributeProvider;

import java.util.UUID;

@Data
public class EvidenceDto {
    private UUID evidenceId;
    private AttributeProvider attributeProvider;
    private Object evidenceData;
}
