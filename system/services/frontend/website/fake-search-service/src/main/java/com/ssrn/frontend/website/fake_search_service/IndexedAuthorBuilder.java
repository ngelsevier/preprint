package com.ssrn.frontend.website.fake_search_service;

public class IndexedAuthorBuilder {
    private String id = "default id";
    private String name = "default name";

    public static IndexedAuthorBuilder anIndexedAuthor() {
        return new IndexedAuthorBuilder();
    }

    private IndexedAuthorBuilder() {
    }

    public IndexedAuthorBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public IndexedAuthorBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IndexedAuthor build() {
        return new IndexedAuthor(this.id, name);
    }
}
