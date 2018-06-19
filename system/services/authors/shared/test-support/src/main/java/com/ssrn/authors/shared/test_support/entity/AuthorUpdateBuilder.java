package com.ssrn.authors.shared.test_support.entity;

import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorUpdate;

import java.util.UUID;

import static com.ssrn.authors.shared.test_support.entity.AuthorBuilder.anAuthor;


public class AuthorUpdateBuilder {
    private String id = UUID.randomUUID().toString();
    private Author author = anAuthor().withId(id).build();

    public static AuthorUpdateBuilder anAuthorUpdate() {
        return new AuthorUpdateBuilder();
    }

    private AuthorUpdateBuilder() {
    }

    public AuthorUpdateBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public AuthorUpdateBuilder withAuthor(AuthorBuilder author) {
        this.author = author.build();
        return this;
    }

    public AuthorUpdate build() {
        return new AuthorUpdate(id, author);
    }
}
