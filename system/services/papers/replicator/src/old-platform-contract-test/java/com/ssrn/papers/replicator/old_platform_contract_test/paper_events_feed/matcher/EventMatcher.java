package com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher;

import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventMatcher extends CustomTypeSafeMatcher<Event> {

    private final int expectedEntityVersion;
    private final String expectedType;

    public static EventMatcher anEvent(String expectedType, int expectedEntityVersion) {
        return new EventMatcher(expectedEntityVersion, expectedType);
    }

    private EventMatcher(int expectedEntityVersion, String expectedType) {
        super(String.format("A %s event with entity version %d", expectedType, expectedEntityVersion));
        this.expectedEntityVersion = expectedEntityVersion;
        this.expectedType = expectedType;
    }

    @Override
    protected boolean matchesSafely(Event event) {
        return event.getEntityVersion() == expectedEntityVersion && expectedType.equals(event.getType());
    }
}
