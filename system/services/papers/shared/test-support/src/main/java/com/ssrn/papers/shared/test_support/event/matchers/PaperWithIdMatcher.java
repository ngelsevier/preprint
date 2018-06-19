package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PaperWithIdMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedPaperId;

    public PaperWithIdMatcher(String expectedPaperId) {
        super(String.format("with id %s", expectedPaperId));
        this.expectedPaperId = expectedPaperId;
    }

    public static Matcher<Paper> id(String expectedPaperId) {
        return new PaperWithIdMatcher(expectedPaperId);
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedPaperId.equals(item.getId());
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("Expected ID '%s' but was '%s'", expectedPaperId, item.getId()));
    }
}
