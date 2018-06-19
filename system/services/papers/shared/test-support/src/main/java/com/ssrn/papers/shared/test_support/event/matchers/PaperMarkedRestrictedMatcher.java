package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperMarkedRestrictedMatcher extends CustomTypeSafeMatcher<Paper> {

    private boolean restrictedPaper;

    public static Matcher<Paper> markedRestricted(boolean restrictedPaper) {
        return new PaperMarkedRestrictedMatcher(restrictedPaper);
    }

    private PaperMarkedRestrictedMatcher(boolean restrictedPaper) {
        super(String.format("isPaperRestricted is %s", restrictedPaper));
        this.restrictedPaper = restrictedPaper;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return restrictedPaper == item.isPaperRestricted();
    }
}
