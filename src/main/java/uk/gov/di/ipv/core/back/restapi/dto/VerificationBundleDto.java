package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;

@Data
@AllArgsConstructor
public class VerificationBundleDto {
    private IdentityVerificationBundle identityVerificationBundle;
}
