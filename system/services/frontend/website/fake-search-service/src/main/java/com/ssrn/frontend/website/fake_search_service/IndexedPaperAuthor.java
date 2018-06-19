package com.ssrn.frontend.website.fake_search_service;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexedPaperAuthor {
    private String id;
    private String name;

    @JsonCreator
    public IndexedPaperAuthor(@JsonProperty("id") String id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}