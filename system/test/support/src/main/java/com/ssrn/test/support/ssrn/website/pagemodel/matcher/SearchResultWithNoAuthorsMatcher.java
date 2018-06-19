package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultWithNoAuthorsMatcher extends CustomTypeSafeMatcher<SearchResult> {

    public static Matcher<SearchResult> noAuthors() {
        return new SearchResultWithNoAuthorsMatcher();
    }

    private SearchResultWithNoAuthorsMatcher() {
        super("with no paper authors");
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        return item.getAuthors() == null || item.getAuthors().length == 0;
    }
}
