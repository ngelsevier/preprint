package com.ssrn.test.support.journey;

import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.junit.Assume.assumeThat;

public class AssumePreviousTestPassedWatcher extends TestWatcher {

    private final PreviousTestResult previousTestResult;

    public AssumePreviousTestPassedWatcher(PreviousTestResult previousTestResult) {
        this.previousTestResult = previousTestResult;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        assumeThat(previousTestResult, new PreviousTestPassedMatcher());
        return super.apply(base, description);
    }

    @Override
    protected void succeeded(Description description) {
        previousTestResult.markAsPassed();
    }

    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        previousTestResult.markAsSkipped();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        previousTestResult.markAsFailed();
    }

    private static class PreviousTestPassedMatcher extends CustomTypeSafeMatcher<PreviousTestResult> {
        public PreviousTestPassedMatcher() {
            super("Previous test passed");
        }

        @Override
        protected boolean matchesSafely(PreviousTestResult previousTestResult) {
            return previousTestResult.passed();
        }
    }
}
