package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssrn.search.domain.AuthorUpdate;

import java.util.Optional;

public class SearchIndexPaperAuthor implements ElasticSearchDocument {
    private String id;
    private String name;

    @JsonCreator
    public SearchIndexPaperAuthor(@JsonProperty("id") String id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<SearchIndexPaperAuthor> cloneIfUpdatedBy(AuthorUpdate.Author updatedAuthor) {
        return updatedAuthor.getName().equals(name) ?
                Optional.empty() :
                Optional.of(new SearchIndexPaperAuthor(id, updatedAuthor.getName()));
    }
}
