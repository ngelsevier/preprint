package com.ssrn.frontend.website.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaperSearchResult extends BaseSearchResult {
    private final String title;
    private final String keywords;
    private SearchResultAuthor[] authors;

    @JsonCreator()
    public PaperSearchResult(@JsonProperty(value = "id", required = true) String id,
                             @JsonProperty(value = "title", required = true) String title,
                             @JsonProperty(value = "keywords") String keywords,
                             @JsonProperty(value = "authors") SearchResultAuthor[] authors) {
        super(id);
        this.title = title;
        this.keywords = keywords;
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public String getKeywords() {
        return keywords;
    }

    public SearchResultAuthor[] getAuthors() {
        return authors;
    }

    @Override
    public String toString() {
        return "PaperSearchResult{" +
                "title='" + title + '\'' +
                ", keywords='" + keywords + '\'' +
                ", authors=" + Arrays.toString(authors) +
                '}';
    }
}
