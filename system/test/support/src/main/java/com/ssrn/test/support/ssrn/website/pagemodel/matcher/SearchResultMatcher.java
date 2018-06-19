package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SearchResultMatcher extends TypeSafeMatcher<SearchResult> {

    private final Matcher<SearchResult> matcher;

    public static Matcher<SearchResult> searchResultWith(Matcher<SearchResult> matcher) {
        return new SearchResultMatcher(matcher);
    }

    private SearchResultMatcher(Matcher<SearchResult> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("search result ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        return matcher.matches(item);
    }
}
