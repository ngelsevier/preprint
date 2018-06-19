package com.ssrn.papers.postgres;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class RetryingRunnable {

    private final TimeUnit timeoutUnit;
    private final int timeout;
    private final Interval interval;
    private final Runnable runnable;
    private final List<Class> nonFatalExceptions;

    public static FluentSyntax tryAndRun(Runnable runnable) {
        return new FluentSyntax(runnable);
    }

    private RetryingRunnable(Runnable runnable, TimeUnit timeoutUnit, int timeout, Interval interval, List<Class> nonFatalExceptions) {
        this.timeoutUnit = timeoutUnit;
        this.timeout = timeout;
        this.interval = interval;
        this.runnable = runnable;
        this.nonFatalExceptions = nonFatalExceptions;
    }

    void run() {
        long timeoutTime = System.currentTimeMillis() + timeoutUnit.toMillis(timeout);

        while (clockHasNotYetPassed(timeoutTime)) {
            try {
                runnable.run();
                return;
            } catch (RuntimeException e) {
                if (nonFatalExceptions.contains(e.getClass())) {
                    FluentSyntax.FluentSyntax2.sleepFor(interval);
                } else {
                    throw e;
                }
            }
        }

        throw new RuntimeException(String.format("One of the following exceptions was still being thrown after %s %s: [%s]",
                timeout, timeoutUnit, formattedListOf(nonFatalExceptions)));
    }

    private static String formattedListOf(List<Class> nonFatalExceptions) {
        return nonFatalExceptions
                .stream()
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    private static boolean clockHasNotYetPassed(long millisecondsSinceEpoch) {
        return System.currentTimeMillis() < millisecondsSinceEpoch;
    }

    public static class FluentSyntax {
        private final Runnable runnable;

        FluentSyntax(Runnable runnable) {
            this.runnable = runnable;
        }

        public FluentSyntax2 retryingOn(Class... nonFatalExceptions) {
            return new FluentSyntax2(runnable, nonFatalExceptions);
        }

        public static class FluentSyntax2 {
            private final Runnable runnable;

            private final Class[] nonFatalExceptions;

            FluentSyntax2(Runnable runnable, Class... nonFatalExceptions) {
                this.runnable = runnable;
                this.nonFatalExceptions = nonFatalExceptions;
            }

            public void forNoMoreThan(int timeout, TimeUnit timeoutUnit, Interval interval) {
                new RetryingRunnable(runnable, timeoutUnit, timeout, interval, asList(nonFatalExceptions)).run();
            }

            static void sleepFor(Interval interval) {
                try {
                    Thread.sleep(interval.getUnit().toMillis(interval.getValue()));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

}
