package uk.gov.di.ipv.core.back.domain;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import lombok.Data;

import java.net.URI;
import java.util.List;

@Data
public class AuthData {
    private URI redirectURI;
    private Scope scope;
    private State state;
    private ResponseMode responseMode;
    private ClientID clientID;
    private AuthorizationCode authorizationCode;
    private List<String> requestedAttributes;
}
