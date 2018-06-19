package com.ssrn.frontend.website.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorSearchResult extends BaseSearchResult {
    private final String name;

    @JsonCreator
    public AuthorSearchResult(@JsonProperty(value = "id", required = true) String id,
                              @JsonProperty(value = "name", required = true) String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AuthorSearchResult{" +
                "id='" + getId() + '\'' +
                " , name='" + name + '\'' +
                '}';
    }
}
