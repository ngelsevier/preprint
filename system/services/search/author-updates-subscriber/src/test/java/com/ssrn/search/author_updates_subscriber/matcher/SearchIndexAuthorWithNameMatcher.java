package com.ssrn.search.author_updates_subscriber.matcher;

import com.ssrn.search.shared.SearchIndexAuthor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchIndexAuthorWithNameMatcher extends CustomTypeSafeMatcher<SearchIndexAuthor> {
    private final String expectedName;

    public static Matcher<SearchIndexAuthor> name(String expectedName) {
        return new SearchIndexAuthorWithNameMatcher(expectedName);
    }

    private SearchIndexAuthorWithNameMatcher(String expectedName) {
        super(String.format("name '%s'", expectedName));
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(SearchIndexAuthor item) {
        return expectedName.equals(item.getName());
    }
}
