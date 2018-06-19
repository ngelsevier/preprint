package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultWithKeywordsMatcher extends CustomTypeSafeMatcher<SearchResult> {
    private final String expectedKeywords;

    public static Matcher<SearchResult> keywords(String expectedKeywords) {
        return new SearchResultWithKeywordsMatcher(expectedKeywords);
    }

    private SearchResultWithKeywordsMatcher(String expectedKeywords) {
        super(String.format("with keywords '%s'", expectedKeywords));
        this.expectedKeywords = expectedKeywords;
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        return expectedKeywords.equals(item.getKeywords());
    }
}
