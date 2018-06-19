package com.ssrn.search.shared;

public class SearchIndexPaperAuthorBuilder {

    private String id = "default id";
    private String name = "default name";

    public static SearchIndexPaperAuthorBuilder aSearchIndexPaperAuthor() {
        return new SearchIndexPaperAuthorBuilder();
    }

    private SearchIndexPaperAuthorBuilder() {
    }

    public SearchIndexPaperAuthorBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SearchIndexPaperAuthorBuilder withNoName() {
        name = null;
        return this;
    }

    public SearchIndexPaperAuthorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public SearchIndexPaperAuthor build() {
        return new SearchIndexPaperAuthor(this.id, name);
    }
}
