package com.ssrn.fake_old_platform;

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