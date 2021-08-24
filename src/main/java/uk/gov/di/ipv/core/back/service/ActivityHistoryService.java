package uk.gov.di.ipv.core.back.service;

import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.ActivityHistoryDto;

public interface ActivityHistoryService {

    Mono<ActivityHistoryDto> processActivityHistory(ActivityHistoryDto activityHistoryDto, SessionData sessionData);
}
