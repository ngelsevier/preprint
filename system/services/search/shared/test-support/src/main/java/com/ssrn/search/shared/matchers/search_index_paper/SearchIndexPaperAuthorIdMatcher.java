package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaperAuthor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SearchIndexPaperAuthorIdMatcher extends CustomTypeSafeMatcher<SearchIndexPaperAuthor> {
    private final String expectedId;

    public static Matcher<SearchIndexPaperAuthor> authorId(String expectedId) {
        return new SearchIndexPaperAuthorIdMatcher(expectedId);
    }

    private SearchIndexPaperAuthorIdMatcher(String expectedId) {
        super(String.format("id '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaperAuthor item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("id was '%s' but expected '%s'", item.getId(), expectedId));
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaperAuthor item) {
        return expectedId.equals(item.getId());
    }
}
