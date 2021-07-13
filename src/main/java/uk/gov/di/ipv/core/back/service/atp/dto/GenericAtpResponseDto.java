package uk.gov.di.ipv.core.back.service.atp.dto;

import com.nimbusds.jose.util.Base64URL;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericAtpResponseDto {
    private boolean genericDataVerified;
    private Object originalData;
    private Base64URL signature;
}
