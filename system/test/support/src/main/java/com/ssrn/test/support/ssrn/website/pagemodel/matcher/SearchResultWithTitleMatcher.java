package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultWithTitleMatcher extends CustomTypeSafeMatcher<SearchResult> {
    private final String expectedTitle;

    public static Matcher<SearchResult> title(String expectedTitle) {
        return new SearchResultWithTitleMatcher(expectedTitle);
    }

    private SearchResultWithTitleMatcher(String expectedTitle) {
        super(String.format("with title '%s'", expectedTitle));
        this.expectedTitle = expectedTitle;
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        return expectedTitle.equals(item.title());
    }
}
