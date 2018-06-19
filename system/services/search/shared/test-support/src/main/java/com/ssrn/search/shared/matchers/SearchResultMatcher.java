package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.BaseSearchResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SearchResultMatcher extends TypeSafeMatcher<BaseSearchResult> {

    private final Matcher<BaseSearchResult> matcher;

    public static Matcher<BaseSearchResult> searchResultWith(Matcher<BaseSearchResult> matcher) {
        return new SearchResultMatcher(matcher);
    }

    private SearchResultMatcher(Matcher<BaseSearchResult> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("search result ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected boolean matchesSafely(BaseSearchResult item) {
        return matcher.matches(item);
    }
}
