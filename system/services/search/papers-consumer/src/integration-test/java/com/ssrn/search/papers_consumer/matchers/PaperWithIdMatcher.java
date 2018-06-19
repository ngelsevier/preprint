package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperWithIdMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedId;

    public static Matcher id(String expectedId) {
        return new PaperWithIdMatcher(expectedId);
    }

    private PaperWithIdMatcher(String expectedId) {
        super(String.format("ID '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedId.equals(item.getId());
    }
}
