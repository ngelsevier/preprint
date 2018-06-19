package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorServiceAuthorUpdate {
    private final String id;
    private final AuthorServiceAuthor author;

    @JsonCreator
    public AuthorServiceAuthorUpdate(@JsonProperty("id") String id, @JsonProperty("author") AuthorServiceAuthor author) {
        this.id = id;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public AuthorServiceAuthor getAuthor() {
        return author;
    }
}
