package com.ssrn.authors.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorUpdate {
    private Author author;
    private String id;

    @JsonCreator
    public AuthorUpdate(@JsonProperty(value = "id", required = true) String id,
                        @JsonProperty(value = "author", required = true) Author author) {
        this.id = id;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public Author getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "AuthorUpdate{" +
                "author=" + author +
                ", id='" + id + '\'' +
                '}';
    }
}
