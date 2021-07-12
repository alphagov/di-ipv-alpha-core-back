package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;

@Data
public class VerificationBundleDto {
    private IdentityVerificationBundle identityVerificationBundle;
}
