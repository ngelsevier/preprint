package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PaperWithKeywordsMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedKeywords;

    public PaperWithKeywordsMatcher(String expectedKeywords) {
        super(String.format("with keywords %s", expectedKeywords));
        this.expectedKeywords = expectedKeywords;
    }

    public static Matcher<Paper> keywords(String expectedKeywords) {
        return new PaperWithKeywordsMatcher(expectedKeywords);
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedKeywords == null ? item.getKeywords() == null : expectedKeywords.equals(item.getKeywords());
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("Expected keywords '%s' but was '%s'", expectedKeywords, item.getKeywords()));
    }
}
