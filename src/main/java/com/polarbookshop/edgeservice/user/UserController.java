package com.polarbookshop.edgeservice.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    /*@GetMapping("user")
    public Mono<User> getUser() {
        return ReactiveSecurityContextHolder.getContext() //Gets SecurityContext for the currently authenticated user from
                                                          // ReactiveSecurityContextHolder
                .map(SecurityContext::getAuthentication) //Gets Authentication from SecurityContext
                .map(authentication ->
                        (OidcUser) authentication.getPrincipal()) //Gets the principal from Authentication. For OIDC, it’s of type OidcUser.
                .map(oidcUser ->
                        new User( //Builds a User object using data from OidcUser (extracted from the ID Token)
                                oidcUser.getPreferredUsername(),
                                oidcUser.getGivenName(),
                                oidcUser.getFamilyName(),
                                List.of("employee", "customer")
                        )
                );
    }*/

    @GetMapping("user")
    public Mono<User> getUser(
            @AuthenticationPrincipal OidcUser oidcUser) { //Injects an OidcUser object containing info about the currently authenticated user
        log.info("Fetching information about the currently authenticated user");
        var user = new User( //Builds a User object from relevant claims contained in OidcUser
                oidcUser.getPreferredUsername(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName(),
                oidcUser.getClaimAsStringList("roles") //Gets the “roles” claim and extracts it as a list of strings
        );
        return Mono.just(user); //Wraps the User object in a reactive publisher, since Edge Service is a reactive application
    }
}
