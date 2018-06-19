package com.ssrn.search.shared;

import java.util.Arrays;
import java.util.UUID;

public class SearchIndexPaperBuilder {

    private String id = UUID.randomUUID().toString();
    private String title = "Default paper title";
    private SearchIndexPaperAuthor[] authors = new SearchIndexPaperAuthor[0];
    private String keywords;

    public static SearchIndexPaperBuilder aSearchIndexPaper() {
        return new SearchIndexPaperBuilder();
    }

    private SearchIndexPaperBuilder() {
    }

    public SearchIndexPaperBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public SearchIndexPaperBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public SearchIndexPaperBuilder withAuthors(SearchIndexPaperAuthorBuilder... authors) {
        this.authors = Arrays.stream(authors).map(SearchIndexPaperAuthorBuilder::build).toArray(SearchIndexPaperAuthor[]::new);
        return this;
    }

    public SearchIndexPaperBuilder withAuthors(SearchIndexPaperAuthor... authors) {
        this.authors = authors;
        return this;
    }

    public SearchIndexPaper build() {
        return new SearchIndexPaper(id, title, keywords, authors);
    }

    public SearchIndexPaperBuilder withKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
}
