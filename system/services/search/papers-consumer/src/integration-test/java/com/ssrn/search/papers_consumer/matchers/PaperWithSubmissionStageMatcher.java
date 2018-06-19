package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class PaperWithSubmissionStageMatcher extends CustomTypeSafeMatcher<Paper> {
    private final String expectedSubmissionStage;

    public static Matcher<Paper> submissionStage(String expectedSubmissionStage) {
        return new PaperWithSubmissionStageMatcher(expectedSubmissionStage);
    }

    private PaperWithSubmissionStageMatcher(String expectedSubmissionStage) {
        super(String.format("submission stage '%s'", expectedSubmissionStage));
        this.expectedSubmissionStage = expectedSubmissionStage;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return item.getSubmissionStage() != null && expectedSubmissionStage.equals(item.getSubmissionStage().getName());
    }
}
