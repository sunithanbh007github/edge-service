package com.polarbookshop.edgeservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class WebEndpoints {

    @Bean //Functional REST endpoints are defined in a bean.
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route() //Offers a fluent API to build routes
                .GET("/catalog-fallback", request -> //Fallback response used to handle the GET endpoint
                        ServerResponse.ok().body(Mono.just(""), String.class))
                .POST("/catalog-fallback", request -> //Fallback response used to handle the POST endpoint
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build())
                .build(); //Builds the functional endpoints
    }
}
