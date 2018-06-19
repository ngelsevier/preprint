package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperMarkedPrivateMatcher extends CustomTypeSafeMatcher<Paper> {

    public static Matcher<Paper> markedPrivate() {
        return new PaperMarkedPrivateMatcher();
    }

    private PaperMarkedPrivateMatcher() {
        super("isPaperPrivate true");
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return item.isPaperPrivate();
    }
}