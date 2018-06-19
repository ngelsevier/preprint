package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class EventWithIdMatcher extends CustomTypeSafeMatcher<Paper.Event> {
    private final String expectedEventId;

    public EventWithIdMatcher(String expectedEventId) {
        super(String.format("with id %s", expectedEventId));
        this.expectedEventId = expectedEventId;
    }

    public static Matcher<Paper.Event> id(String expectedEventId) {
        return new EventWithIdMatcher(expectedEventId);
    }

    @Override
    protected boolean matchesSafely(Paper.Event item) {
        return expectedEventId.equals(item.getId());
    }
}
