package com.polarbookshop.edgeservice.user;

import java.util.List;

public record User( //Immutable data class holding user data
        String username,
        String firstName,
        String lastName,
        List<String> roles
) {}
