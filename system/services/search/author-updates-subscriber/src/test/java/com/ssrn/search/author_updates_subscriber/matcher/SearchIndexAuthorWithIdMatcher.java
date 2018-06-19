package com.ssrn.search.author_updates_subscriber.matcher;

import com.ssrn.search.shared.SearchIndexAuthor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchIndexAuthorWithIdMatcher extends CustomTypeSafeMatcher<SearchIndexAuthor> {
    private final String expectedId;

    public static Matcher<SearchIndexAuthor> id(String expectedId) {
        return new SearchIndexAuthorWithIdMatcher(expectedId);
    }

    private SearchIndexAuthorWithIdMatcher(String expectedId) {
        super(String.format("id '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(SearchIndexAuthor item) {
        return expectedId.equals(item.getId());
    }
}
