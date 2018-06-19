package com.ssrn.frontend.website.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResults {
    private final int totalNumberOfResults;
    private final BaseSearchResult[] searchResults;

    SearchResults(@JsonProperty(value = "totalNumberOfResults") int totalNumberOfResults, @JsonProperty(value = "results") BaseSearchResult[] searchResults) {
        this.totalNumberOfResults = totalNumberOfResults;
        this.searchResults = searchResults;
    }

    public BaseSearchResult[] getSearchResults() {
        return searchResults;
    }

    public int getTotalNumberOfResults() {
        return totalNumberOfResults;
    }
}