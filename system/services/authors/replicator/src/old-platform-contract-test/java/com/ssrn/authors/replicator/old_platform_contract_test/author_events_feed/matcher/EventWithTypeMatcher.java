package com.ssrn.authors.replicator.old_platform_contract_test.author_events_feed.matcher;

import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithTypeMatcher extends CustomTypeSafeMatcher<Event> {

    private final String expectedType;

    public static EventWithTypeMatcher anEventWithType(String expectedType) {
        return new EventWithTypeMatcher(expectedType);
    }

    private EventWithTypeMatcher(String expectedType) {
        super(String.format("A %s event", expectedType));
        this.expectedType = expectedType;
    }

    @Override
    protected boolean matchesSafely(Event event) {
        return expectedType.equals(event.getType());
    }
}
