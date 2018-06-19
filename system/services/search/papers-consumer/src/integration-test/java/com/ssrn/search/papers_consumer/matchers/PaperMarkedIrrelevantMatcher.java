package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperMarkedIrrelevantMatcher extends CustomTypeSafeMatcher<Paper> {

    public static Matcher<Paper> markedIrrelevant() {
        return new PaperMarkedIrrelevantMatcher();
    }

    private PaperMarkedIrrelevantMatcher() {
        super("isPaperIrrelevant true");
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return item.isPaperIrrelevant();
    }
}