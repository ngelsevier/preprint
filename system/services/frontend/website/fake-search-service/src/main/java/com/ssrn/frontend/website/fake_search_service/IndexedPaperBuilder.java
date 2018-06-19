package com.ssrn.frontend.website.fake_search_service;

import java.util.Arrays;
import java.util.UUID;

import static com.ssrn.frontend.website.fake_search_service.IndexedPaperAuthorBuilder.anIndexedPaperAuthor;

public class IndexedPaperBuilder {
    private String id = UUID.randomUUID().toString();
    private String title = "default title";
    private IndexedPaperAuthor[] authors = new IndexedPaperAuthor[]{ anIndexedPaperAuthor().build() };
    private String keywords;

    public static IndexedPaperBuilder anIndexedPaper() {
        return new IndexedPaperBuilder();
    }

    private IndexedPaperBuilder() {
    }

    public IndexedPaperBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public IndexedPaperBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IndexedPaperBuilder withAuthors(IndexedPaperAuthorBuilder... authors) {
        this.authors = Arrays.stream(authors).map(IndexedPaperAuthorBuilder::build).toArray(IndexedPaperAuthor[]::new);
        return this;
    }

    public IndexedPaperBuilder withKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    public IndexedPaper build() {
        return new IndexedPaper(id, title, authors, keywords);
    }

}
