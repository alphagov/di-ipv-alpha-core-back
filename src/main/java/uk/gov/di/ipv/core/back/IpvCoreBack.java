package uk.gov.di.ipv.core.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class})
public class IpvCoreBack {

    public static void main(String[] args) {
        SpringApplication.run(IpvCoreBack.class, args);
    }

}
