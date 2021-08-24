package uk.gov.di.ipv.core.back.service.impl;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.gpg45.Score;
import uk.gov.di.ipv.core.back.restapi.dto.IdentityVerificationDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.Gpg45Service;
import uk.gov.di.ipv.core.back.service.SessionService;
import uk.gov.di.ipv.core.back.service.VerificationService;

@Service
public class VerificationServiceImpl implements VerificationService {

    private final Gpg45Service gpg45Service;
    private final SessionService sessionService;

    public VerificationServiceImpl(
        Gpg45Service gpg45Service,
        SessionService sessionService
    ) {
        this.gpg45Service = gpg45Service;
        this.sessionService = sessionService;
    }

    @Override
    public Mono<IdentityVerificationDto> processVerification(IdentityVerificationDto identityVerificationDto, SessionData sessionData) {
        var score = Score.NOT_AVAILABLE;

        switch (identityVerificationDto.getType()) {
            case SELFIE_CHECK:
                score = Score.FOUR;
                break;
            case CRA_CHECK:
                score = Score.THREE;
                break;
            case MNO_CHECK:
                score = Score.TWO;
                break;
        }

        identityVerificationDto.setVerificationScore(score);
        sessionData.getIdentityVerificationBundle()
            .getBundleScores()
            .setIdentityVerificationScore(score);

        var verificationBundle = new VerificationBundleDto(sessionData.getIdentityVerificationBundle());
        return gpg45Service.calculate(verificationBundle)
            .map(response -> sessionService.saveAndReturnSessionDto(response, sessionData))
            .map(_sessionData -> identityVerificationDto);
    }
}
