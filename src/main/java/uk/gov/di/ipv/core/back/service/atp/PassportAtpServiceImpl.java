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
import uk.gov.di.ipv.core.back.service.atp.dto.PassportAtpResponseDto;

import java.text.ParseException;

@Slf4j
@Service("passport-atp")
public class PassportAtpServiceImpl implements AtpService {

    private final WebClient passportClient;

    public PassportAtpServiceImpl(
        @Qualifier("passport-client") WebClient passportClient
    ) {
        this.passportClient = passportClient;
    }

    @Override
    public Mono<IdentityEvidence> processEvidence(EvidenceDto evidenceDto) {
        var monoDto = passportClient.post()
            .uri("/process")
            .bodyValue(evidenceDto.getEvidenceData())
            .exchangeToMono(clientResponse -> {
                if (clientResponse.statusCode().value() != HttpStatus.OK.value()) {
                    log.error(
                        "Something went wrong whilst posting to passport ATP, status: {}, reason: {}",
                        clientResponse.statusCode().value(),
                        clientResponse.statusCode().getReasonPhrase());
                    throw new RuntimeException("Not OK value from passport ATP");
                }

                return clientResponse.bodyToMono(String.class);
            }).flatMap(PassportAtpServiceImpl::toDto);

        return monoDto.map(dto -> {
            var identityEvidence = new IdentityEvidence();
            identityEvidence.setUuid(evidenceDto.getEvidenceId());
            identityEvidence.setType(EvidenceType.UK_PASSPORT);
            identityEvidence.setEvidenceData(dto);

            return identityEvidence;
        });
    }

    // TODO: extract this method into a generic method somewhere
    private static Mono<PassportAtpResponseDto> toDto(String stringResponse) {
        try {
            var jws = JWSObject.parse(stringResponse);
            // TODO: verify JWS somewhere in here
            var decoded = jws.getPayload().toJSONObject();
            var isValid = decoded.get("isValid");

            return Mono.just(new PassportAtpResponseDto((boolean) isValid, jws.getSignature()));
        } catch (ParseException exception) {
            log.error("Failed to decode jws object");
            throw new RuntimeException("Failed to decode jws object", exception);
        }
    }
}
