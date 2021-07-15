package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.domain.gpg45.IdentityProfile;

@Data
public class CalculateResponseDto {
    private IdentityVerificationBundle identityVerificationBundle;
    private IdentityProfile matchedIdentityProfile;
}
