package uk.gov.di.ipv.core.back.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.restapi.dto.CalculateResponseDto;
import uk.gov.di.ipv.core.back.restapi.dto.VerificationBundleDto;
import uk.gov.di.ipv.core.back.service.Gpg45Service;

@Slf4j
@Service
public class Gpg45ServiceImpl implements Gpg45Service {

    private final WebClient gpg45Client;

    @Autowired
    public Gpg45ServiceImpl(
        @Qualifier("gpg-45-client") WebClient gpg45Client
    ) {
        this.gpg45Client = gpg45Client;
    }

    @Override
    public Mono<CalculateResponseDto> calculate(VerificationBundleDto identityVerificationBundle) {

        return gpg45Client.post()
            .uri("/calculate")
            .bodyValue(identityVerificationBundle)
            .exchangeToMono(clientResponse -> {
                if (clientResponse.statusCode().value() != HttpStatus.OK.value()) {
                    log.error(
                        "Something went wrong whilst posting to GPG45 engine, status: {}, reason: {}",
                        clientResponse.statusCode().value(),
                        clientResponse.statusCode().getReasonPhrase());
                    throw new RuntimeException("Not OK value from GPG45 engine");
                }

                return clientResponse.bodyToMono(CalculateResponseDto.class);
            });
    }
}
