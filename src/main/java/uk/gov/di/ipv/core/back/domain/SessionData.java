package uk.gov.di.ipv.core.back.domain;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.State;
import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.IdentityVerificationBundle;
import uk.gov.di.ipv.core.back.domain.gpg45.IdentityProfile;

import java.net.URI;
import java.util.UUID;

@Data
public class SessionData {
    private UUID sessionId;
    private IpvRoute previousRoute;
    private IdentityVerificationBundle identityVerificationBundle;
    private IdentityProfile identityProfile;

    //TODO: Split this out into its own class?
    private URI redirectURI;
    private Scope scope;
    private State state;
    private ResponseMode responseMode;
//    private AuthorizationRequest authorizationRequest;
    private AuthorizationCode authorizationCode;
}
