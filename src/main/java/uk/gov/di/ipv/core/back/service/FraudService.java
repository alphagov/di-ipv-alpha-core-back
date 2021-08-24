package uk.gov.di.ipv.core.back.service;

import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.restapi.dto.FraudCheckDto;
import uk.gov.di.ipv.core.back.restapi.dto.IdentityVerificationDto;

public interface FraudService {

    Mono<FraudCheckDto> processFraudCheck(FraudCheckDto fraudCheckDto, SessionData sessionData);
}
