package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AuthorEntitiesPage {
    private List<Author> authors;

    @JsonCreator
    public AuthorEntitiesPage(@JsonProperty(value="authors", required = true) List<Author> authorsInAscendingIdOrder) {
        this.authors = authorsInAscendingIdOrder;
    }

    public List<Author> getAuthors() {
        return authors;
    }
}
