package com.ssrn.papers.replicator.page_item_stream;

import java.util.Arrays;
import java.util.stream.Collectors;

class LinkedPageBuilder {

    private String name = "default name";
    private String nextPageName = "default next page name";
    private LinkedPageItem[] linkedPageItems;

    static LinkedPageBuilder linkedPageNamed(String name) {
        return new LinkedPageBuilder(name);
    }

    private LinkedPageBuilder(String name) {
        this.name = name;
    }

    LinkedPageBuilder withNextPageName(String nextPageName) {
        this.nextPageName = nextPageName;
        return this;
    }

    LinkedPageBuilder withItems(LinkedPageItemBuilder... pageItemBuilders) {
        this.linkedPageItems = Arrays.stream(pageItemBuilders).map(LinkedPageItemBuilder::build).collect(Collectors.toList()).toArray(new LinkedPageItem[0]);
        return this;
    }

    LinkedPage build() {
        return new LinkedPage(name, nextPageName, linkedPageItems);
    }
}
