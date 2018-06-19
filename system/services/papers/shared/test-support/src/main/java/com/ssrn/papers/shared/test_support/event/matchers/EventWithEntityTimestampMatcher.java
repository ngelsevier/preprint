package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.DateTime;

public class EventWithEntityTimestampMatcher extends CustomTypeSafeMatcher<Paper.Event> {
    private final DateTime expectedTime;

    private EventWithEntityTimestampMatcher(DateTime expectedTime) {
        super(String.format("with entity timestamp set to %s", expectedTime));
        this.expectedTime = expectedTime;
    }

    public static EventWithEntityTimestampMatcher entitySameTimestampAndTimezoneAs(DateTime expectedTime) {
        return new EventWithEntityTimestampMatcher(expectedTime);
    }

    @Override
    protected boolean matchesSafely(Paper.Event item) {
        return item.getEntityTimestamp() != null && item.getEntityTimestamp().equals(expectedTime);
    }
}
