package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchIndexAuthor implements ElasticSearchDocument {
    private String id;
    private String name;

    @JsonCreator
    public SearchIndexAuthor(@JsonProperty("id") String id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SearchIndexAuthor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
