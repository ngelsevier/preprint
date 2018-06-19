package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.CustomTypeSafeMatcher;

public class SearchResultAuthorWithNameMatcher extends CustomTypeSafeMatcher<SearchResult.Author> {
    private String authorName;

    public static CustomTypeSafeMatcher<SearchResult.Author> withName(String authorName) {
        return new SearchResultAuthorWithNameMatcher(authorName);
    }

    private SearchResultAuthorWithNameMatcher(String authorName) {
        super(String.format("a search result author with name %s", authorName));
        this.authorName = authorName;
    }

    @Override
    protected boolean matchesSafely(SearchResult.Author searchResultAuthor) {
        return searchResultAuthor != null && authorName.equals(searchResultAuthor.getName());
    }
}