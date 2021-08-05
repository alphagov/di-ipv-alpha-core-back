package uk.gov.di.ipv.core.back.domain;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.domain.gpg45.ConfidenceLevel;
import uk.gov.di.ipv.core.back.domain.gpg45.IdentityProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class SessionData {
    private UUID sessionId;
    private IpvRoute previousRoute;
    private IdentityVerificationBundle identityVerificationBundle;
    private IdentityProfile identityProfile;
    private AuthData authData;
    private ConfidenceLevel requestedLevelOfConfidence;
    private Map<AttributeName, List<String>> collectedAttributes;
}
