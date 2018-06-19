package com.ssrn.test.support.journey;

public class PreviousTestResult {
    private boolean passed;
    private boolean noPreviousTest = true;

    public static PreviousTestResult noPreviousTest() {
        return new PreviousTestResult();
    }

    private PreviousTestResult() {
    }

    public boolean passed() {
        return noPreviousTest || passed;
    }

    public void markAsPassed() {
        recordPassed(true);
    }

    public void markAsFailed() {
        recordPassed(false);
    }

    public void markAsSkipped() {
        recordPassed(false);
    }

    protected void recordPassed(boolean passed) {
        this.passed = passed;
        noPreviousTest = false;
    }

    @Override
    public String toString() {
        return "PreviousTestResult{" +
                "passed=" + passed +
                ", noPreviousTest=" + noPreviousTest +
                '}';
    }
}
