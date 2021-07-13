package uk.gov.di.ipv.core.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AtpConfig {

    private final String passportAtpEndpoint;
    private final String genericAtpEndpoint;

    public AtpConfig(
        @Value("${atp.passport.endpoint}") String passportAtpEndpoint,
        @Value("${atp.generic.endpoint}") String genericAtpEndpoint
    ) {
        this.passportAtpEndpoint = passportAtpEndpoint;
        this.genericAtpEndpoint = genericAtpEndpoint;
    }

    @Bean("passport-client")
    WebClient passportClient() {
        return WebClient.builder()
            .baseUrl(passportAtpEndpoint)
            .build();
    }

    @Bean("generic-client")
    WebClient genericClient() {
        return WebClient.builder()
            .baseUrl(genericAtpEndpoint)
            .build();
    }
}
