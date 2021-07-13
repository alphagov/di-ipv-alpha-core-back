package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SessionDataDto {
    private UUID sessionId;
}
