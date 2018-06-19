package com.ssrn.frontend.website.search;

import com.ssrn.frontend.website.BasePageView;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.function.Function;

public class SearchResultsPageView extends BasePageView {

    private final String searchQuery;
    private final BaseSearchResult[] searchResults;
    private final Function<BaseSearchResult, String> searchResultToArticlePageUrlMapper;
    private final Function<SearchResultAuthor, String> searchResultAuthorToAuthorPageUrlMapper;
    private final Function<SearchResultAuthor, String> searchResultAuthorToAuthorImageUrlMapper;
    private int totalNumberOfSearchResults;
    private int searchResultsOffsetFrom;
    private int searchResultsPerPage;

    SearchResultsPageView(BaseSearchResult[] searchResults, String searchQuery,
                          Function<BaseSearchResult, String> searchResultToArticlePageUrlMapper,
                          Function<SearchResultAuthor, String> searchResultAuthorToAuthorPageUrlMapper,
                          Function<SearchResultAuthor, String> searchResultAuthorToAuthorImageUrlMapper,
                          String authBaseUrl, int totalNumberOfSearchResults, int searchResultsOffsetFrom, int searchResultPageSize) {
        super("results.ftl", authBaseUrl);
        this.searchResults = searchResults;
        this.searchQuery = searchQuery;
        this.searchResultToArticlePageUrlMapper = searchResultToArticlePageUrlMapper;
        this.searchResultAuthorToAuthorPageUrlMapper = searchResultAuthorToAuthorPageUrlMapper;
        this.searchResultAuthorToAuthorImageUrlMapper = searchResultAuthorToAuthorImageUrlMapper;
        this.totalNumberOfSearchResults = totalNumberOfSearchResults;
        this.searchResultsOffsetFrom = searchResultsOffsetFrom;
        this.searchResultsPerPage = searchResultPageSize;
    }

    public SearchResultViewModel[] getSearchResults() {
        return Arrays.stream(searchResults)
                .map(searchResult -> new SearchResultViewModel(
                        searchResult,
                        searchResultToArticlePageUrlMapper,
                        searchResultAuthorToAuthorPageUrlMapper, searchResultAuthorToAuthorImageUrlMapper)
                ).toArray(SearchResultViewModel[]::new);
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getNumberOfSearchResults() {
        return NumberFormat.getIntegerInstance().format(totalNumberOfSearchResults);
    }

    public String getNextFrom() {
        return String.valueOf(searchResultsOffsetFrom + searchResultsPerPage);
    }

    public String getPrevFrom() {
        return String.valueOf(searchResultsOffsetFrom - searchResultsPerPage);
    }

    public boolean getShowNext() {
        return (searchResultsOffsetFrom + searchResultsPerPage) < totalNumberOfSearchResults;
    }

    public boolean getShowPrev() {
        return searchResultsOffsetFrom > 0;
    }
}
