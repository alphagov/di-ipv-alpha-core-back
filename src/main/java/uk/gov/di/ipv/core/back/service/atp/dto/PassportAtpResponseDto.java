package uk.gov.di.ipv.core.back.service.atp.dto;

import com.nimbusds.jose.util.Base64URL;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PassportAtpResponseDto {
    private boolean isValid;
    private Base64URL signature;
}
