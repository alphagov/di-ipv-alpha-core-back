package uk.gov.di.ipv.core.back.service;

import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;

import java.util.UUID;

public interface RoutingService {

    RouteDto getNextRoute(UUID sessionId);
}
