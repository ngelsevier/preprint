package com.ssrn.frontend.website.search;

public interface SearchEngine {
    SearchResults findItemsMatching(String query, int searchResultsOffsetFrom);
}
