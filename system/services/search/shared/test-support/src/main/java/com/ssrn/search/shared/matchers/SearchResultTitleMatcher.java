package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.BaseSearchResult;
import com.ssrn.search.shared.PaperSearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultTitleMatcher extends CustomTypeSafeMatcher<BaseSearchResult> {
    private final String expectedTitle;

    public static Matcher<BaseSearchResult> paperTitle(String expectedTitle) {
        return new SearchResultTitleMatcher(expectedTitle);
    }

    private SearchResultTitleMatcher(String expectedTitle) {
        super(String.format("with title '%s'", expectedTitle));
        this.expectedTitle = expectedTitle;
    }

    @Override
    protected boolean matchesSafely(BaseSearchResult item) {
        return expectedTitle.equals(((PaperSearchResult)item).getTitle());
    }
}
