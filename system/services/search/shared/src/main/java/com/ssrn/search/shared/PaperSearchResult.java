package com.ssrn.search.shared;

import java.util.Arrays;
import java.util.Map;

public class PaperSearchResult extends BaseSearchResult {

    private final String title;
    private SearchResultAuthor[] authors;
    private String type = "Paper";
    private String keywords;

    PaperSearchResult(String id, String title, String keywords, SearchResultAuthor[] authors, Map<String, String[]> highlightedFields) {
        super(id, highlightedFields);

        this.title = findHighlightedFields("title", title, highlightedFields);
        this.keywords = findHighlightedFields("keywords", keywords, highlightedFields);
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public SearchResultAuthor[] getAuthors() {
        return authors;
    }

    public String getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "PaperSearchResult{" +
                "title='" + title + '\'' +
                ", authors=" + Arrays.toString(authors) +
                ", keywords='" + keywords + '\'' +
                ", id='" + id + '\'' +
                ", highlightedFields=" + highlightedFields +
                '}';
    }
}
