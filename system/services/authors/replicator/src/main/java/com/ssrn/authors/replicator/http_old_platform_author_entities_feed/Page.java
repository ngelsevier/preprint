package com.ssrn.authors.replicator.http_old_platform_author_entities_feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

    private List<Author> authors;

    public Page(@JsonProperty("authors") List<Author> authors) {
        this.authors = authors;
    }

    public List<Author> getAuthors() {
        return authors;
    }
}
