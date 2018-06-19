package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperMarkedRestrictedMatcher extends CustomTypeSafeMatcher<Paper> {

    public static Matcher<Paper> markedRestricted() {
        return new PaperMarkedRestrictedMatcher();
    }

    private PaperMarkedRestrictedMatcher() {
        super("isPaperRestricted true");
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return item.isPaperRestricted();
    }
}