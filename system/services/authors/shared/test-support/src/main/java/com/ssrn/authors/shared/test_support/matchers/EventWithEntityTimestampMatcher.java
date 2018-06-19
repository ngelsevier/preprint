package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Event;
import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.DateTime;

public class EventWithEntityTimestampMatcher extends CustomTypeSafeMatcher<Event> {
    private final DateTime expectedTime;

    private EventWithEntityTimestampMatcher(DateTime expectedTime) {
        super(String.format("with entity timestamp set to %s", expectedTime));
        this.expectedTime = expectedTime;
    }

    public static EventWithEntityTimestampMatcher entitySameTimestampAndTimezoneAs(DateTime expectedTime) {
        return new EventWithEntityTimestampMatcher(expectedTime);
    }

    @Override
    protected boolean matchesSafely(Event item) {
        return item.getEntityTimestamp() != null && item.getEntityTimestamp().equals(expectedTime);
    }
}
