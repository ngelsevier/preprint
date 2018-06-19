package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SearchIndexPaperKeywordsMatcher extends CustomTypeSafeMatcher<SearchIndexPaper> {
    private final String expectedKeywords;

    public static Matcher<SearchIndexPaper> paperKeywords(String expectedKeywords) {
        return new SearchIndexPaperKeywordsMatcher(expectedKeywords);
    }

    private SearchIndexPaperKeywordsMatcher(String expectedKeywords) {
        super(String.format("title '%s'", expectedKeywords));
        this.expectedKeywords = expectedKeywords;
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaper item) {
        return expectedKeywords == null ? item.getKeywords() == null : expectedKeywords.equals(item.getKeywords());
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("paperKeywords were '%s' but expected '%s'", item.getKeywords(), expectedKeywords));
    }
}
