package com.ssrn.test.support.standalone_test_runner;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ConsoleOutputListener extends RunListener {

    private static final int RED = 31;
    private static final int GREEN = 32;
    private static final int YELLOW = 33;

    @Override
    public void testStarted(Description description) throws Exception {
        printTestEvent("started", description, GREEN);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        printTestEvent("failed", failure.getDescription(), RED);
        printColouredLine(RED, ExceptionUtils.getStackTrace(failure.getException()));
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        printTestEvent("ignored", description, YELLOW);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        String testSummary = String.format("%d tests completed, %d failed, %d skipped.",
                result.getRunCount() + result.getIgnoreCount(),
                result.getFailureCount(),
                result.getIgnoreCount()
        );

        printColouredLine(result.wasSuccessful() ? GREEN : RED, testSummary);
        System.out.println(String.format("Total time: %f secs ", result.getRunTime() / 1000f));
    }

    private void printTestEvent(String event, Description description, int colourCode) {
        printColouredLine(colourCode, String.format("Test %s : %s > %s", event, description.getClassName(), description.getMethodName()));
    }

    private void printColouredLine(int colourCode, String text) {
        System.out.println(String.format((char) 27 + "[%dm" + text + (char) 27 + "[0m", colourCode));
    }
}
