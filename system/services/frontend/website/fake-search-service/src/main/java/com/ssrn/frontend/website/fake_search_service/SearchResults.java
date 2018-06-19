package com.ssrn.frontend.website.fake_search_service;

public class SearchResults {
    private final int totalNumberOfResults;
    private final BaseSearchResult[] results;

    SearchResults(int totalNumberOfResults, BaseSearchResult[] results) {
        this.totalNumberOfResults = totalNumberOfResults;
        this.results = results;
    }

    public int getTotalNumberOfResults() {
        return totalNumberOfResults;
    }

    public BaseSearchResult[] getResults() {
        return results;
    }
}