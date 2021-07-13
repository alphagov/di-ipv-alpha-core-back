package uk.gov.di.ipv.core.back.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;
import uk.gov.di.ipv.core.back.service.RoutingService;

@Service
public class RoutingServiceImpl implements RoutingService {
    @Override
    public RouteDto getNextRoute() {
        return null;
    }
}
