package com.ssrn.frontend.website.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaperSearchResult.class, name = "Paper"),
        @JsonSubTypes.Type(value = AuthorSearchResult.class, name = "Author")
})
public abstract class BaseSearchResult {

    private final String id;

    BaseSearchResult(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
