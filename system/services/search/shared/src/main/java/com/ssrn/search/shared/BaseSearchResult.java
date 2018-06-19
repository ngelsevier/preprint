package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaperSearchResult.class, name = "Paper"),
        @JsonSubTypes.Type(value = AuthorSearchResult.class, name = "Author")
})
public abstract class BaseSearchResult {
    protected final String id;
    protected final Map<String, String[]> highlightedFields;

    public BaseSearchResult(String id, Map<String, String[]> highlightedFields) {
        this.id = id;
        this.highlightedFields = highlightedFields;
    }

    public String getId() {
        return id;
    }

    public Map<String, String[]> getHighlightedFields() {
        return highlightedFields;
    }

    String findHighlightedFields(String fieldName, String fieldValue, Map<String, String[]> highlightedFields) {
        String[] hightlighted = highlightedFields.get(fieldName);
        return hightlighted != null ? hightlighted[0] : fieldValue;
    }
}
