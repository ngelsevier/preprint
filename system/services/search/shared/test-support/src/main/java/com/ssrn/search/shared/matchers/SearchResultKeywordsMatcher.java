package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.BaseSearchResult;
import com.ssrn.search.shared.PaperSearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultKeywordsMatcher extends CustomTypeSafeMatcher<BaseSearchResult> {
    private final String expectedKeywords;

    public static Matcher<BaseSearchResult> paperKeywords(String expectedKeywords) {
        return new SearchResultKeywordsMatcher(expectedKeywords);
    }

    private SearchResultKeywordsMatcher(String expectedKeywords) {
        super(String.format("with paperKeywords '%s'", expectedKeywords));
        this.expectedKeywords = expectedKeywords;
    }

    @Override
    protected boolean matchesSafely(BaseSearchResult item) {
        return expectedKeywords.equals(((PaperSearchResult)item).getKeywords());
    }
}
