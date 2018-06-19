package com.ssrn.frontend.website;

import java.security.Principal;

public class User implements Principal {
    private final String name;

    User(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}