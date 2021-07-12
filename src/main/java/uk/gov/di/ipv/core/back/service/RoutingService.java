package uk.gov.di.ipv.core.back.service;

import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;

public interface RoutingService {

    RouteDto getNextRoute();
}
