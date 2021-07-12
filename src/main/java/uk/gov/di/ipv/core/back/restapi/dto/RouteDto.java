package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.IpvPath;

import java.util.UUID;

@Data
public class RouteDto {
    UUID sessionId;
    UUID correlationId;
    IpvPath route;
}
