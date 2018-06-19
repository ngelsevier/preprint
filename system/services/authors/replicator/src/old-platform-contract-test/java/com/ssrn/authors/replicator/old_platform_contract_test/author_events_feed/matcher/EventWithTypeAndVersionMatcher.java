package com.ssrn.authors.replicator.old_platform_contract_test.author_events_feed.matcher;

import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithTypeAndVersionMatcher extends CustomTypeSafeMatcher<Event> {

    private final int expectedEntityVersion;
    private final String expectedType;

    public static EventWithTypeAndVersionMatcher anEventWithTypeAndVersion(String expectedType, int expectedEntityVersion) {
        return new EventWithTypeAndVersionMatcher(expectedEntityVersion, expectedType);
    }

    private EventWithTypeAndVersionMatcher(int expectedEntityVersion, String expectedType) {
        super(String.format("A %s event with entity version %d", expectedType, expectedEntityVersion));
        this.expectedEntityVersion = expectedEntityVersion;
        this.expectedType = expectedType;
    }

    @Override
    protected boolean matchesSafely(Event event) {
        return event.getEntityVersion() == expectedEntityVersion && expectedType.equals(event.getType());
    }
}
