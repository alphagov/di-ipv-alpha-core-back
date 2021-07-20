package uk.gov.di.ipv.core.back.domain;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.domain.gpg45.IdentityProfile;

import java.util.UUID;

@Data
public class SessionData {
    private UUID sessionId;
    private IpvRoute previousRoute;
    private IdentityVerificationBundle identityVerificationBundle;
    private IdentityProfile identityProfile;
    private AuthData authData;
}
