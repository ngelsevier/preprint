package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticsearchSearchHitsSource {
    private final String id;
    private final String title;
    private String keywords;
    private SearchIndexPaperAuthor[] authors;
    private final String name;

    @JsonCreator
    public ElasticsearchSearchHitsSource(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("keywords") String keywords,
            @JsonProperty("authors") SearchIndexPaperAuthor[] authors,
            @JsonProperty("name") String name
    ) {
        this.id = id;
        this.title = title;
        this.keywords = keywords;
        this.authors = authors;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SearchIndexPaperAuthor[] getAuthors() {
        return authors;
    }

    public String getName() {
        return name;
    }

    public String getKeywords() {
        return keywords;
    }
}
