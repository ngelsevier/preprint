package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultWithNoKeywordsMatcher extends CustomTypeSafeMatcher<SearchResult> {

    public static Matcher<SearchResult> noKeywords() {
        return new SearchResultWithNoKeywordsMatcher();
    }

    private SearchResultWithNoKeywordsMatcher() {
        super("with no paper keywords");
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        return item.getKeywords() == null || "".equals(item.getKeywords());
    }
}
