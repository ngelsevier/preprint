package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventOfTypeMatcher extends CustomTypeSafeMatcher<Event> {
    private final String expectedType;

    public static EventOfTypeMatcher type(String expectedType) {
        return new EventOfTypeMatcher(expectedType);
    }

    private EventOfTypeMatcher(String expectedType) {
        super(String.format("of type '%s'", expectedType));
        this.expectedType = expectedType;
    }

    @Override
    protected boolean matchesSafely(Event actual) {
        return expectedType.equals(actual.getType());
    }
}
