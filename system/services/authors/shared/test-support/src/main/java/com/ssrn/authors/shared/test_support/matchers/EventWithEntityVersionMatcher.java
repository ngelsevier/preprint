package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithEntityVersionMatcher extends CustomTypeSafeMatcher<Event> {
    private final int expectedEntityVersion;

    public static EventWithEntityVersionMatcher entityVersion(int expectedEntityVersion) {
        return new EventWithEntityVersionMatcher(expectedEntityVersion);
    }

    private EventWithEntityVersionMatcher(int expectedEntityVersion) {
        super(String.format("with entity version %s", expectedEntityVersion));
        this.expectedEntityVersion = expectedEntityVersion;
    }

    @Override
    protected boolean matchesSafely(Event item) {
        return expectedEntityVersion == item.getEntityVersion();
    }
}
