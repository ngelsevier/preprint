package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.SearchResultAuthor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultAuthorNameMatcher extends CustomTypeSafeMatcher<SearchResultAuthor> {

    private final String expectedName;

    public static Matcher<SearchResultAuthor> authorName(String expectedName) {
        return new SearchResultAuthorNameMatcher(expectedName);
    }

    private SearchResultAuthorNameMatcher(String expectedName) {
        super(String.format("with name '%s'", expectedName));
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(SearchResultAuthor item) {
        if (expectedName == null){
            return item.getName() == null;
        }

        return expectedName.equals(item.getName());
    }
}
