package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorServiceAuthor {
    private final String id;
    private final String name;

    private final boolean removed;

    @JsonCreator
    public AuthorServiceAuthor(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("removed") boolean removed) {
        this.id = id;
        this.name = name;
        this.removed = removed;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isRemoved() {
        return removed;
    }
}
