package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import java.util.UUID;

public class AuthorServiceAuthorUpdateBuilder {

    private String id = UUID.randomUUID().toString();
    private String name;
    private boolean removed;

    public static AuthorServiceAuthorUpdateBuilder anAuthorUpdate() {
        return new AuthorServiceAuthorUpdateBuilder();
    }

    public AuthorServiceAuthorUpdateBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public AuthorServiceAuthorUpdateBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AuthorServiceAuthorUpdateBuilder withRemoved(boolean removed) {
        this.removed = removed;
        return this;
    }

    public AuthorServiceAuthorUpdate build() {
        return new AuthorServiceAuthorUpdate(id, new AuthorServiceAuthor(id, name, removed));
    }
}
