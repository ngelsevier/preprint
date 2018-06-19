package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

import java.util.Arrays;

public class SearchResultWithAuthorNamesMatcher extends CustomTypeSafeMatcher<SearchResult> {
    private final String[] expectedAuthorNames;

    public static Matcher<SearchResult> authorNamesInOrder(String... expectedAuthorNames) {
        return new SearchResultWithAuthorNamesMatcher(expectedAuthorNames);
    }

    private SearchResultWithAuthorNamesMatcher(String... expectedAuthorNames) {
        super(String.format("with authors in order '%s'", expectedAuthorNames));
        this.expectedAuthorNames = expectedAuthorNames;
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        String[] actualAuthorNames = item.getAuthors() != null ? Arrays.stream(item.getAuthors()).map(SearchResult.Author::getName).toArray(String[]::new) : null;
        return Arrays.equals(this.expectedAuthorNames, actualAuthorNames);
    }
}
