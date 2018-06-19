package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.SearchResultAuthor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SearchResultAuthorMatcher extends TypeSafeMatcher<SearchResultAuthor> {
    private final Matcher<SearchResultAuthor> matcher;

    public static Matcher<SearchResultAuthor> paperAuthor(Matcher<SearchResultAuthor> matcher) {
        return new SearchResultAuthorMatcher(matcher);
    }

    private SearchResultAuthorMatcher(Matcher<SearchResultAuthor> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("author ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected boolean matchesSafely(SearchResultAuthor item) {
        return matcher.matches(item);
    }
}
