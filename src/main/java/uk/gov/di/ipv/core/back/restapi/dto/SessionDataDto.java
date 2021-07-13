package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Builder;
import lombok.Data;
import uk.gov.di.ipv.core.back.domain.IpvRoute;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.domain.gpg45.IdentityProfile;

import java.util.UUID;

@Data
@Builder
public class SessionDataDto {
    private UUID sessionId;
    private IpvRoute previousRoute;
    private IdentityVerificationBundle identityVerificationBundle;
    private IdentityProfile identityProfile;

    public static SessionDataDto fromSessionData(SessionData sessionData) {
        return SessionDataDto.builder()
            .sessionId(sessionData.getSessionId())
            .previousRoute(sessionData.getPreviousRoute())
            .identityVerificationBundle(sessionData.getIdentityVerificationBundle())
            .identityProfile(sessionData.getIdentityProfile())
            .build();
    }
}
