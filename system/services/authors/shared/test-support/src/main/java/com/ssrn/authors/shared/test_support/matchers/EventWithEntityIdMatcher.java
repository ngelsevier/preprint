package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithEntityIdMatcher extends CustomTypeSafeMatcher<Event> {
    private final String expectedEntityId;

    public static EventWithEntityIdMatcher entityId(String expectedEntityId) {
        return new EventWithEntityIdMatcher(expectedEntityId);
    }

    private EventWithEntityIdMatcher(String expectedEntityId) {
        super(String.format("for entity %s", expectedEntityId));
        this.expectedEntityId = expectedEntityId;
    }

    @Override
    protected boolean matchesSafely(Event actual) {
        return expectedEntityId.equals(actual.getEntityId());
    }
}
