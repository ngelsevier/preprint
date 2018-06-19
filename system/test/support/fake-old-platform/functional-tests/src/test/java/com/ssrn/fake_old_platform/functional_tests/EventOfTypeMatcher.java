package com.ssrn.fake_old_platform.functional_tests;

import org.hamcrest.CustomTypeSafeMatcher;

public class EventOfTypeMatcher extends CustomTypeSafeMatcher<Event> {
    private final String expectedType;

    public static EventOfTypeMatcher ofType(String expectedType) {
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
