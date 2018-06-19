package com.ssrn.papers.replicator.page_item_stream;

class LinkedPage {
    private final String name;
    private final String nextPageName;
    private final LinkedPageItem[] linkedPageItems;

    LinkedPage(String name, String nextPageName, LinkedPageItem[] linkedPageItems) {
        this.name = name;
        this.nextPageName = nextPageName;
        this.linkedPageItems = linkedPageItems;
    }

    String getName() {
        return name;
    }

    String getNextPageName() {
        return nextPageName;
    }

    public LinkedPageItem[] getItems() {
        return linkedPageItems;
    }
}
