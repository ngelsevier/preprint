package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;

public class AuthorUpdateBuilder {

    private AuthorUpdate.Author author = anAuthor().build();

    public static AuthorUpdateBuilder anAuthorUpdate() {
        return new AuthorUpdateBuilder();
    }

    private AuthorUpdateBuilder() {
    }

    public AuthorUpdateBuilder withAuthor(AuthorBuilder author) {
        this.author = author.build();
        return this;
    }

    public AuthorUpdateBuilder withAuthor(AuthorUpdate.Author author) {
        this.author = author;
        return this;
    }

    public AuthorUpdate build() {
        return new AuthorUpdate(author.getId(), author);
    }
}
