package uk.gov.di.ipv.core.back.service.atp;

import com.nimbusds.jose.JWSObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.di.ipv.core.back.domain.data.EvidenceType;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.restapi.dto.EvidenceDto;
import uk.gov.di.ipv.core.back.service.AtpService;
import uk.gov.di.ipv.core.back.service.atp.dto.GenericAtpResponseDto;

import java.text.ParseException;

@Slf4j
@Service("generic-atp")
public class GenericAtpServiceImpl implements AtpService {

    private final WebClient genericClient;

    public GenericAtpServiceImpl(
        @Qualifier("generic-client") WebClient genericClient
    ) {
        this.genericClient = genericClient;
    }

    @Override
    public Mono<IdentityEvidence> processEvidence(EvidenceDto evidenceDto) {
        var monoDto = genericClient.post()
            .uri("/process")
            .bodyValue(evidenceDto.getEvidenceData())
            .exchangeToMono(clientResponse -> {
                if (clientResponse.statusCode().value() != HttpStatus.OK.value()) {
                    log.error(
                        "Something went wrong whilst posting to generic ATP, status: {}, reason: {}",
                        clientResponse.statusCode().value(),
                        clientResponse.statusCode().getReasonPhrase());
                    throw new RuntimeException("Not OK value from generic ATP");
                }

                return clientResponse.bodyToMono(String.class);
            })
            .flatMap(GenericAtpServiceImpl::toDto);

        return monoDto.flatMap(dto -> {
            var identityEvidence = new IdentityEvidence();
            identityEvidence.setEvidenceData(dto);
            identityEvidence.setUuid(evidenceDto.getEvidenceId());
            identityEvidence.setType(EvidenceType.ATP_GENERIC_DATA);

            return Mono.just(identityEvidence);
        });
    }

    private static Mono<GenericAtpResponseDto> toDto(String stringResponse) {
        try {
            var jws = JWSObject.parse(stringResponse);
            // TODO: verify JWS somewhere in here
            var decoded = jws.getPayload().toJSONObject();
            var originalData = decoded.get("originalData");
            var isVerified = decoded.get("genericDataVerified");

            return Mono.just(new GenericAtpResponseDto((boolean) isVerified, originalData, jws.getSignature()));
        } catch (ParseException exception) {
            log.error("Failed to decode jws object");
            throw new RuntimeException("Failed to decode jws object", exception);
        }
    }
}
