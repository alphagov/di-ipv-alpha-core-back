package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.gpg45.Score;
import uk.gov.di.ipv.core.back.restapi.dto.ActivityHistoryDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.ActivityHistoryService;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.SessionService;

@Slf4j
@Service
public class ActivityHistoryServiceImpl implements ActivityHistoryService {

    private final Gpg45Service gpg45Service;
    private final SessionService sessionService;

    public ActivityHistoryServiceImpl(
        Gpg45Service gpg45Service,
        SessionService sessionService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
    }


    @Override
    public Mono<ActivityHistoryDto> processActivityHistory(ActivityHistoryDto activityHistoryDto, SessionData sessionData) {
        var score = activityHistoryDto.getActivityHistoryScore();

        if (score == null) {
            score = Score.NOT_AVAILABLE;
        }

        log.info("Setting activity history score of {}", score);
        sessionData.getIdentityVerificationBundle()
            .getBundleScores()
            .setActivityCheckScore(score);

        log.info("Sending and saving bundle to gpg45");
        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        return gpg45Service.calculate(verificationBundle)
            .map(response -> sessionService.saveAndReturnSessionDto(response, sessionData))
            .map(_sessionData -> activityHistoryDto);
    }
}
