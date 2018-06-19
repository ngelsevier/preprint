package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.SubmissionStage;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PaperWithSubmissionStageMatcher extends CustomTypeSafeMatcher<Paper> {
    private final SubmissionStage expectedSubmissionStage;

    public PaperWithSubmissionStageMatcher(SubmissionStage expectedSubmissionStage) {
        super(String.format("with submissionStage %s", expectedSubmissionStage));
        this.expectedSubmissionStage = expectedSubmissionStage;
    }

    public static Matcher<Paper> submissionStage(SubmissionStage expectedSubmissionStage) {
        return new PaperWithSubmissionStageMatcher(expectedSubmissionStage);
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return expectedSubmissionStage.equals(item.getSubmissionStage());
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("Expected submissionStage '%s' but was '%s'", expectedSubmissionStage, item.getSubmissionStage()));
    }
}
