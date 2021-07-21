package uk.gov.di.ipv.core.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Gpg45Config {

    private @Value("${gpg45.endpoint}") String gpg45Endpoint;

    @Bean("gpg-45-client")
    WebClient gpg45Client() {
        return WebClient.builder()
            .baseUrl(gpg45Endpoint)
            .build();
    }
}
