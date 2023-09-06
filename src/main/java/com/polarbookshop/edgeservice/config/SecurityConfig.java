package com.polarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean //Defines a repository to store Access Tokens in the web session
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean //The SecurityWebFilterChain bean is used to define and configure security policies for the application.
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        return http
                .authorizeExchange(exchange ->
                        exchange
                                .pathMatchers("/actuator/**").permitAll()
                                //Allows unauthenticated access to the SPA static resources
                                .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
                                //Allows unauthenticated read access to the books in the catalog
                                .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
                                .anyExchange().authenticated() //Any other request requires user authentication.
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint( //When an exception is thrown because a user is not authenticated,
                                //it replies with an HTTP 401 response.
                                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(Customizer.withDefaults()) //Enables user authentication with OAuth2/OpenID Connect
                .logout(logout -> logout.logoutSuccessHandler( //Defines a custom handler for the scenario
                        // where a logout operation is completed successfully
                        oidcLogoutSuccessHandler(clientRegistrationRepository)))
                //Uses a cookie-based strategy for exchanging CSRF tokens with the Angular frontend
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler()))

                .build();
    }



    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}"); //After logging out from the OIDC Provider,
                                                                        // Keycloak will redirect the user to the application base URL
                                                                // computed dynamically from Spring (locally, it’s http://localhost:9000).
        return oidcLogoutSuccessHandler;
    }

    @Bean
    WebFilter csrfWebFilter() { //A filter with the only purpose of subscribing to the CsrfToken reactive stream and ensuring its
                                 //value is extracted correctly
        return (exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
                Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
                return csrfToken != null ? csrfToken.then() : Mono.empty();
            }));
            return chain.filter(exchange);
        };
    }
}

