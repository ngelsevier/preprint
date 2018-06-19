package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SearchIndexPaperTitleMatcher extends CustomTypeSafeMatcher<SearchIndexPaper> {
    private final String expectedTitle;

    public static Matcher<SearchIndexPaper> paperTitle(String expectedTitle) {
        return new SearchIndexPaperTitleMatcher(expectedTitle);
    }

    private SearchIndexPaperTitleMatcher(String expectedTitle) {
        super(String.format("title '%s'", expectedTitle));
        this.expectedTitle = expectedTitle;
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaper item) {
        return item.getTitle().equals(expectedTitle);
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("title was '%s' but expected '%s'", item.getTitle(), expectedTitle));
    }
}
