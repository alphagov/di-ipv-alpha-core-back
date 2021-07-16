package uk.gov.di.ipv.core.back.config;

import com.nimbusds.oauth2.sdk.id.ClientID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpvConfig {

    @Bean("ipv-client-id")
    ClientID clientID() {
        return new ClientID("some-client-id");
    }
}
