package com.ssrn.authors.shared.test_support.entity;

import com.ssrn.authors.domain.Author;

import java.util.UUID;

public class AuthorBuilder {
    private String id = UUID.randomUUID().toString();
    private String name = "default name";
    private int version = 1;
    private boolean removed;

    public static AuthorBuilder anAuthor() {
        return new AuthorBuilder();
    }

    private AuthorBuilder() {
    }

    public AuthorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public AuthorBuilder withVersion(int version) {
        this.version = version;
        return this;
    }


    public AuthorBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AuthorBuilder withRemoval(boolean removed) {
        this.removed = removed;
        return this;
    }

    public Author build() {
        return new Author(id, version, name, removed);
    }
}
