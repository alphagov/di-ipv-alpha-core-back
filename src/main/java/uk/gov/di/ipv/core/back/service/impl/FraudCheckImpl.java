package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.gpg45.Score;
import uk.gov.di.ipv.core.back.restapi.dto.FraudCheckDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.FraudService;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.SessionService;

@Slf4j
@Service
public class FraudCheckImpl implements FraudService {

    private final Gpg45Service gpg45Service;
    private final SessionService sessionService;

    public FraudCheckImpl(
        Gpg45Service gpg45Service,
        SessionService sessionService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
    }

    @Override
    public Mono<FraudCheckDto> processFraudCheck(FraudCheckDto fraudCheckDto, SessionData sessionData) {
        var score = fraudCheckDto.getFraudCheckScore();

        if (score == null) {
            score = Score.NOT_AVAILABLE;
        }

        log.info("Set a score of {} for fraud check", score);

        sessionData.getIdentityVerificationBundle()
            .getBundleScores()
            .setFraudCheckScore(score);

        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        log.info("Sending and saving bundle to gpg45");
        return gpg45Service.calculate(verificationBundle)
            .map(response -> sessionService.saveAndReturnSessionDto(response, sessionData))
            .map(_sessionData -> fraudCheckDto);
    }
}
