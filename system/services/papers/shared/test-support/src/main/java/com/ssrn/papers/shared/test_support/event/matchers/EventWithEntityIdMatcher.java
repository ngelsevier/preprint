package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithEntityIdMatcher extends CustomTypeSafeMatcher<Paper.Event> {
    private final String expectedEntityId;

    public static EventWithEntityIdMatcher entityId(String expectedEntityId) {
        return new EventWithEntityIdMatcher(expectedEntityId);
    }

    private EventWithEntityIdMatcher(String expectedEntityId) {
        super(String.format("for entity %s", expectedEntityId));
        this.expectedEntityId = expectedEntityId;
    }

    @Override
    protected boolean matchesSafely(Paper.Event actual) {
        return expectedEntityId.equals(actual.getEntityId());
    }
}
