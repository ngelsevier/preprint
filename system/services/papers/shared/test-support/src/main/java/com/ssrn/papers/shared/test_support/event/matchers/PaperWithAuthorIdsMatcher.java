package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Arrays;

public class PaperWithAuthorIdsMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String[] expectedAuthorIds;

    public PaperWithAuthorIdsMatcher(String[] expectedAuthorIds) {
        super(String.format("with authorIds %s", Arrays.toString(expectedAuthorIds)));
        this.expectedAuthorIds = expectedAuthorIds;
    }

    public static Matcher<Paper> authorIds(String... expectedAuthorIds) {
        return new PaperWithAuthorIdsMatcher(expectedAuthorIds);
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return Arrays.equals(expectedAuthorIds, item.getAuthorIds());
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("Expected authorIds '%s' but was '%s'", Arrays.toString(expectedAuthorIds), Arrays.toString(item.getAuthorIds())));
    }
}
