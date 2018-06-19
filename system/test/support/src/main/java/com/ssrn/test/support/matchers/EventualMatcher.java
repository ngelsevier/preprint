package com.ssrn.test.support.matchers;

import com.ssrn.test.support.utils.Interval;
import com.ssrn.test.support.utils.ThreadingUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;

public class EventualMatcher<T> extends TypeSafeMatcher<Supplier<T>> {
    private final int timeout;
    private final TimeUnit timeoutUnit;
    private final int interval;
    private final TimeUnit intervalUnit;
    private final Matcher<T> matcher;
    private T lastItemSupplied;
    private Throwable lastExceptionThrown;

    public static <T> FluentSyntax<T> eventuallySatisfies(Matcher<T> matcher) {
        return new FluentSyntax<>(matcher);
    }

    EventualMatcher(Matcher<T> matcher, int timeout, TimeUnit timeoutUnit, int interval, TimeUnit intervalUnit) {
        this.matcher = matcher;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    @Override
    public void describeTo(Description description) {
        description.appendDescriptionOf(matcher);
        description.appendText(String.format(" within %d %s (checking every %d %s)", timeout, timeoutUnit.toString().toLowerCase(), interval, intervalUnit.toString().toLowerCase()));
    }

    @Override
    protected void describeMismatchSafely(Supplier<T> supplier, Description mismatchDescription) {
        mismatchDescription.appendText("the last time we checked, ");

        if (lastExceptionThrown != null) {
            mismatchDescription.appendText(String.format("the supplier threw an exception: %s", ExceptionUtils.getFullStackTrace(lastExceptionThrown)));
        } else {
            matcher.describeMismatch(lastItemSupplied, mismatchDescription);
        }

    }

    @Override
    protected boolean matchesSafely(Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        long timeoutInMilliseconds = timeoutUnit.toMillis(timeout);

        while (System.currentTimeMillis() - startTime < timeoutInMilliseconds) {
            lastExceptionThrown = null;

            try {
                lastItemSupplied = supplier.get();

                if (matcher.matches(lastItemSupplied)) {
                    return true;
                }
            } catch (Throwable throwable) {
                lastExceptionThrown = throwable;
            }

            sleepFor(interval, intervalUnit);
        }

        return false;
    }

    public static class FluentSyntax<T> {
        private final Matcher<T> matcher;

        FluentSyntax(Matcher<T> matcher) {
            this.matcher = matcher;
        }

        public EventualMatcher<T> within(int timeout, TimeUnit timeoutUnit, Interval interval) {
            return new EventualMatcher<>(matcher, timeout, timeoutUnit, interval.getValue(), interval.getUnit());
        }
    }

}
