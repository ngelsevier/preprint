package com.ssrn.fake_old_platform.functional_tests;

import org.hamcrest.CustomTypeSafeMatcher;

public class EventForEntityMatcher extends CustomTypeSafeMatcher<Event> {
    private final String expectedEntityId;

    public static EventForEntityMatcher forEntity(String expectedEntityId) {
        return new EventForEntityMatcher(expectedEntityId);
    }

    private EventForEntityMatcher(String expectedEntityId) {
        super(String.format("for entity %s", expectedEntityId));
        this.expectedEntityId = expectedEntityId;
    }

    @Override
    protected boolean matchesSafely(Event actual) {
        return expectedEntityId.equals(actual.getEntityId());
    }
}
