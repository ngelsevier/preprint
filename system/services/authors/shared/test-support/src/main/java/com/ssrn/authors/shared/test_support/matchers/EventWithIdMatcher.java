package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Event;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class EventWithIdMatcher extends CustomTypeSafeMatcher<Event> {
    private final String expectedEventId;

    public EventWithIdMatcher(String expectedEventId) {
        super(String.format("with id %s", expectedEventId));
        this.expectedEventId = expectedEventId;
    }

    public static Matcher<Event> id(String expectedEventId) {
        return new EventWithIdMatcher(expectedEventId);
    }

    @Override
    protected boolean matchesSafely(Event item) {
        return expectedEventId.equals(item.getId());
    }
}
