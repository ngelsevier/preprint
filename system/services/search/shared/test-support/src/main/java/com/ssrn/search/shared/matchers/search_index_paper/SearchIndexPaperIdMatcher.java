package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;

public class SearchIndexPaperIdMatcher extends CustomTypeSafeMatcher<SearchIndexPaper> {
    private final String expectedId;

    public static CustomTypeSafeMatcher<SearchIndexPaper> paperId(final String expectedId) {
        return new SearchIndexPaperIdMatcher(expectedId);
    }

    private SearchIndexPaperIdMatcher(String expectedId) {
        super(String.format("id '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaper item) {
        return expectedId.equals(item.getId());
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("id was '%s' but expected '%s'", item.getId(), expectedId));
    }
}
