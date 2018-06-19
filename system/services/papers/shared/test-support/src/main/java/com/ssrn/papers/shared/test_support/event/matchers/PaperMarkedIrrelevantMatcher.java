package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperMarkedIrrelevantMatcher extends CustomTypeSafeMatcher<Paper> {

    private boolean irrelevantPaper;

    public static Matcher<Paper> markedIrrelevant(boolean irrelevantPaper) {
        return new PaperMarkedIrrelevantMatcher(irrelevantPaper);
    }

    private PaperMarkedIrrelevantMatcher(boolean irrelevantPaper) {
        super(String.format("isPaperIrrelevant is %s", irrelevantPaper));
        this.irrelevantPaper = irrelevantPaper;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return irrelevantPaper == item.isPaperIrrelevant();
    }
}
