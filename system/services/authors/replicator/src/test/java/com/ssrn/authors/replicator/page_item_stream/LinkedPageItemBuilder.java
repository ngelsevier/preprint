package com.ssrn.authors.replicator.page_item_stream;

public class LinkedPageItemBuilder {

    private String name = "default name";

    static LinkedPageItemBuilder linkedPageItemNamed(String name) {
        return new LinkedPageItemBuilder(name);
    }

    private LinkedPageItemBuilder(String name) {
        this.name = name;
    }

    LinkedPageItem build() {
        return new LinkedPageItem(name);
    }
}
