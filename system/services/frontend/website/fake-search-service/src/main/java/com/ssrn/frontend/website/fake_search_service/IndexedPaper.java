package com.ssrn.frontend.website.fake_search_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexedPaper {
    private final String id;
    private final String title;
    private IndexedPaperAuthor[] authors;
    private final String keywords;

    @JsonCreator
    public IndexedPaper(@JsonProperty("id") String id,
                        @JsonProperty("title") String title,
                        @JsonProperty("authors") IndexedPaperAuthor[] authors,
                        @JsonProperty("keywords") String keywords) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.keywords = keywords;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public IndexedPaperAuthor[] getAuthors() {
        return authors;
    }

    public String getKeywords() {
        return keywords;
    }
}
