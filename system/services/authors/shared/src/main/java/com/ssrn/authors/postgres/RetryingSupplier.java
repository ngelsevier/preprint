package com.ssrn.authors.postgres;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class RetryingSupplier<T> {

    private final TimeUnit timeoutUnit;
    private final int timeout;
    private final Interval interval;
    private final Supplier<T> supplier;
    private final List<Class> nonFatalExceptions;

    public static <T> FluentSyntax<T> tryToSupply(Supplier<T> supplier) {
        return new FluentSyntax<>(supplier);
    }

    private RetryingSupplier(Supplier<T> supplier, TimeUnit timeoutUnit, int timeout, Interval interval, List<Class> nonFatalExceptions) {
        this.timeoutUnit = timeoutUnit;
        this.timeout = timeout;
        this.interval = interval;
        this.supplier = supplier;
        this.nonFatalExceptions = nonFatalExceptions;
    }

    public T get() {
        long timeoutTime = System.currentTimeMillis() + timeoutUnit.toMillis(timeout);

        while (clockHasNotYetPassed(timeoutTime)) {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                if (nonFatalExceptions.contains(e.getClass())) {
                    FluentSyntax.FluentSyntax2.sleepFor(interval);
                } else {
                    throw e;
                }
            }
        }

        throw new TimeoutException(timeout, timeoutUnit, nonFatalExceptions);
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

    public static class FluentSyntax<T> {
        private final Supplier<T> supplier;

        FluentSyntax(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public FluentSyntax2<T> retryingOn(Class... nonFatalExceptions) {
            return new FluentSyntax2<>(supplier, nonFatalExceptions);
        }

        public static class FluentSyntax2<T> {
            private final Supplier<T> supplier;

            private final Class[] nonFatalExceptions;

            FluentSyntax2(Supplier<T> supplier, Class... nonFatalExceptions) {
                this.supplier = supplier;
                this.nonFatalExceptions = nonFatalExceptions;
            }

            public T forNoMoreThan(int timeout, TimeUnit timeoutUnit, Interval interval) {
                return new RetryingSupplier<>(supplier, timeoutUnit, timeout, interval, asList(nonFatalExceptions)).get();
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

    static class TimeoutException extends RuntimeException {
        TimeoutException(int timeout, TimeUnit timeoutUnit, List<Class> nonFatalExceptions) {
            super(String.format("One of the following exceptions was still being thrown after %s %s: [%s]",
                    timeout, timeoutUnit, formattedListOf(nonFatalExceptions)));
        }
    }
}
