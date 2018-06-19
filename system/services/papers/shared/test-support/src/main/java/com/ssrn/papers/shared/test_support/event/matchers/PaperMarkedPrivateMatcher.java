package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PaperMarkedPrivateMatcher extends CustomTypeSafeMatcher<Paper> {

    private boolean privatePaper;

    public static Matcher<Paper> markedPrivate(boolean privatePaper) {
        return new PaperMarkedPrivateMatcher(privatePaper);
    }

    private PaperMarkedPrivateMatcher(boolean privatePaper) {
        super(String.format("isPaperPrivate is %s", privatePaper));
        this.privatePaper = privatePaper;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return privatePaper == item.isPaperPrivate();
    }
}
