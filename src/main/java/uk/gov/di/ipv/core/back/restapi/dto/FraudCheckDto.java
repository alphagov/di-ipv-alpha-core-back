package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.gpg45.Score;

@Data
public class FraudCheckDto {

    private Object fraudCheckData;

    private Score fraudCheckScore;
}
