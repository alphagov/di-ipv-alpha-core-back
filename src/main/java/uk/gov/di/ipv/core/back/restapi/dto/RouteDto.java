package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.di.ipv.core.back.domain.IpvRoute;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RouteDto {
    UUID sessionId;
//    UUID correlationId;
    IpvRoute route;
}
