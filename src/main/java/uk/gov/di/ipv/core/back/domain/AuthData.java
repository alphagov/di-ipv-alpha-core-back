package uk.gov.di.ipv.core.back.domain;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.State;
import lombok.Data;

import java.net.URI;

@Data
public class AuthData {
    private URI redirectURI;
    private Scope scope;
    private State state;
    private ResponseMode responseMode;
    private AuthorizationRequest authorizationRequest;
    private AuthorizationCode authorizationCode;
}
