package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaperAuthor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SearchIndexPaperAuthorNameMatcher extends CustomTypeSafeMatcher<SearchIndexPaperAuthor> {
    private final String expectedName;

    public static Matcher<SearchIndexPaperAuthor> authorName(String expectedName) {
        return new SearchIndexPaperAuthorNameMatcher(expectedName);
    }

    private SearchIndexPaperAuthorNameMatcher(String expectedName) {
        super(String.format("name '%s'", expectedName));
        this.expectedName = expectedName;
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaperAuthor item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("name was '%s' but expected '%s' ", item.getName(), expectedName));
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaperAuthor item) {
        return (expectedName == null && item.getName() == null) || expectedName.equals(item.getName());
    }
}
