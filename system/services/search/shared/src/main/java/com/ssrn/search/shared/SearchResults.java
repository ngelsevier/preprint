package com.ssrn.search.shared;

public class SearchResults {
    private final int totalNumberOfResults;
    private final BaseSearchResult[] results;

    SearchResults(int totalNumberOfResults, BaseSearchResult[] results) {
        this.totalNumberOfResults = totalNumberOfResults;
        this.results = results;
    }

    public BaseSearchResult[] getResults() {
        return results;
    }

    public int getTotalNumberOfResults() {
        return totalNumberOfResults;
    }
}
