package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.SearchResultAuthor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultAuthorIdMatcher extends CustomTypeSafeMatcher<SearchResultAuthor> {

    private final String expectedId;

    public static Matcher<SearchResultAuthor> authorId(String expectedId) {
        return new SearchResultAuthorIdMatcher(expectedId);
    }

    private SearchResultAuthorIdMatcher(String expectedId) {
        super(String.format("with id '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(SearchResultAuthor item) {
        return expectedId.equals(item.getId());
    }
}
