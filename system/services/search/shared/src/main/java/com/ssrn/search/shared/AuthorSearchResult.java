package com.ssrn.search.shared;

import java.util.HashMap;

public class AuthorSearchResult extends BaseSearchResult {

    private final String name;
    private String type = "Author";

    AuthorSearchResult(String id, String name, HashMap<String, String[]> highlightedFields) {
        super(id, highlightedFields);
        this.name = findHighlightedFields("name", name, highlightedFields);
    }

    public String getName() {
        String[] highlightedAuthorNames = getHighlightedFields().get("name");
        return highlightedAuthorNames != null ? highlightedAuthorNames[0] : name;
    }

    @Override
    public String toString() {
        return "AuthorSearchResult{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", id='" + getId() + '\'' +
                '}';
    }
}
