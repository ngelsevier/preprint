package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperWithTitleMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedTitle;

    public static Matcher<Paper> title(String expectedTitle) {
        return new PaperWithTitleMatcher(expectedTitle);
    }

    private PaperWithTitleMatcher(String expectedTitle) {
        super(String.format("title '%s'", expectedTitle));
        this.expectedTitle = expectedTitle;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedTitle.equals(item.getTitle());
    }
}
