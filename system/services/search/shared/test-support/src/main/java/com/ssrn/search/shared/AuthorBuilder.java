package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;

import java.util.UUID;

public class AuthorBuilder {
    private String id = UUID.randomUUID().toString();
    private String name = "default name";
    private boolean removed;

    public static AuthorBuilder anAuthor() {
        return new AuthorBuilder();
    }

    public AuthorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public AuthorBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AuthorBuilder withNoName() {
        name = null;
        return this;
    }

    public AuthorBuilder withRemoved(boolean removed) {
        this.removed = removed;
        return this;
    }

    public AuthorUpdate.Author build() {
        return new AuthorUpdate.Author(id, name, removed);
    }
}
