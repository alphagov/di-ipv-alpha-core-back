package uk.gov.di.ipv.core.back.service;

import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.restapi.dto.CalculateResponseDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;

public interface Gpg45Service {

    Mono<CalculateResponseDto> calculate(VerificationBundleDto identityVerificationBundle);
}
