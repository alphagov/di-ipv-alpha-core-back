package uk.gov.di.ipv.core.back.restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.domain.gpg45.IdentityProfile;

@Data
@Builder
public class UserInfoDto {

    @JsonProperty("sub")
    private String subject;

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("aud")
    private String audience;

    @JsonProperty("identityProfile")
    private IdentityProfile identityProfile;

    @JsonProperty("identityVerificationBundle")
    private IdentityVerificationBundle identityVerificationBundle;
}
