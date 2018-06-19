package com.ssrn.search.shared;

public class SearchIndexAuthorBuilder {

    private String id = "default id";
    private String name = "default name";

    public static SearchIndexAuthorBuilder anAuthor() {
        return new SearchIndexAuthorBuilder();
    }

    private SearchIndexAuthorBuilder() {
    }

    public SearchIndexAuthorBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SearchIndexAuthorBuilder withNoName() {
        name = null;
        return this;
    }

    public SearchIndexAuthorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public SearchIndexAuthor build() {
        return new SearchIndexAuthor(this.id, name);
    }
}
