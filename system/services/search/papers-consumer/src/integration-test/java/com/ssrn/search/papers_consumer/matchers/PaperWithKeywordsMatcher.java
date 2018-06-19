package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperWithKeywordsMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedKeywords;

    public static Matcher<Paper> keywords(String expectedTitle) {
        return new PaperWithKeywordsMatcher(expectedTitle);
    }

    private PaperWithKeywordsMatcher(String expectedKeywords) {
        super(String.format("keywords '%s'", expectedKeywords));
        this.expectedKeywords = expectedKeywords;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedKeywords == null ? item.getKeywords() == null : expectedKeywords.equals(item.getKeywords());
    }
}
