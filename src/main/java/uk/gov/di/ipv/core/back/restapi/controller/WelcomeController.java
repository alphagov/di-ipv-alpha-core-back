package uk.gov.di.ipv.core.back.restapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class WelcomeController {

    @GetMapping
    public Mono<ServerResponse> hello() {
        return ServerResponse
            .ok()
            .body(BodyInserters.fromValue("IPV Core Back Service"));
    }
}
