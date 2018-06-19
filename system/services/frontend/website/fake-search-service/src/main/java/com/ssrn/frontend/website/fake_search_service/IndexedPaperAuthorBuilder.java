package com.ssrn.frontend.website.fake_search_service;

public class IndexedPaperAuthorBuilder {

    private String id = "default id";
    private String name = "default name";

    public static IndexedPaperAuthorBuilder anIndexedPaperAuthor() {
        return new IndexedPaperAuthorBuilder();
    }

    private IndexedPaperAuthorBuilder() {
    }

    public IndexedPaperAuthorBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public IndexedPaperAuthorBuilder withNoName() {
        name = null;
        return this;
    }

    public IndexedPaperAuthorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IndexedPaperAuthor build() {
        return new IndexedPaperAuthor(this.id, name);
    }
}
