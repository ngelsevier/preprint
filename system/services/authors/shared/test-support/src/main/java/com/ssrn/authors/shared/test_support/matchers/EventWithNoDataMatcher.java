package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Event;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithNoDataMatcher extends CustomTypeSafeMatcher<Event> {

    public static EventWithNoDataMatcher noData() {
        return new EventWithNoDataMatcher();
    }

    private EventWithNoDataMatcher() {
        super("with no data");
    }

    @Override
    protected boolean matchesSafely(Event actual) {
        return actual.getData() == null;
    }
}
