package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.di.ipv.core.back.domain.IpvRoute;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.RouteDto;
import uk.gov.di.ipv.core.back.service.RoutingService;
import uk.gov.di.ipv.core.back.service.SessionService;

import java.util.UUID;

@Slf4j
@Service
public class RoutingServiceImpl implements RoutingService {

    private final SessionService sessionService;

    @Autowired
    public RoutingServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public RouteDto getNextRoute(UUID sessionId) {
        var maybeSessionData = sessionService.getSession(sessionId);

        if (maybeSessionData.isEmpty()) {
            log.error("Session data is missing, session id: {}", sessionId);
            throw new RuntimeException("Missing session data");
        }

        var sessionData = maybeSessionData.get();
        var nextRoute = getNextFromSessionData(sessionData);

        return new RouteDto(sessionId, nextRoute);
    }

    private IpvRoute getNextFromSessionData(SessionData sessionData) {
        var route = IpvRoute.HOME;

//        if (sessionData.getPreviousRoute() == null) {
//            sessionData.setPreviousRoute(route);
//            sessionService.saveSession(sessionData);
//            return IpvRoute.HOME;
//        }

//        switch (sessionData.getPreviousRoute()) {
//            case HOME:
//                route = IpvRoute.INFORMATION;
//                break;
//            case INFORMATION:
//                route = IpvRoute.PASSPORT;
//                break;
//            case PASSPORT:
//                route = IpvRoute.ORCHESTRATOR;
//                break;
//            default:
//                route = IpvRoute.ERROR;
//        }

        sessionData.setPreviousRoute(route);
        sessionService.saveSession(sessionData);
        return route;
    }
}
