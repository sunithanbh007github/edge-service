package com.polarbookshop.edgeservice.config;

import java.security.Principal;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver keyResolver() {
        //return exchange -> Mono.just("anonymous"); //Rate limiting is applied to requests using a constant key.

        return exchange -> exchange.getPrincipal() //Gets the currently authenticated user (the principal) from the current request
                                                    // (the exchange)
                                .map(Principal::getName) //Extracts the username from the principal
                                    .defaultIfEmpty("anonymous"); //If the request is unauthenticated,
                                                                            // it uses “anonymous” as the default key to apply rate-limiting.
    }
}
