package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PaperWithTitleMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedPaperTitle;

    public PaperWithTitleMatcher(String expectedTitle) {
        super(String.format("with title %s", expectedTitle));
        this.expectedPaperTitle = expectedTitle;
    }

    public static Matcher<Paper> title(String expectedTitle) {
        return new PaperWithTitleMatcher(expectedTitle);
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedPaperTitle.equals(item.getTitle());
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("Expected title '%s' but was '%s'", expectedPaperTitle, item.getTitle()));
    }
}
