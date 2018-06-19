package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PaperWithVersionMatcher extends CustomTypeSafeMatcher<Paper> {
    private final int expectedVersion;

    private PaperWithVersionMatcher(int expectedVersion) {
        super(String.format("with version %d", expectedVersion));
        this.expectedVersion = expectedVersion;
    }

    public static Matcher<Paper> version(int expectedVersion) {
        return new PaperWithVersionMatcher(expectedVersion);
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedVersion == item.getVersion();
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("Expected version '%d' but was '%d'", expectedVersion, item.getVersion()));
    }
}
