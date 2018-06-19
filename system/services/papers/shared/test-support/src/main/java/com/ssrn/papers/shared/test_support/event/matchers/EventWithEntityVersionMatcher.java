package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithEntityVersionMatcher extends CustomTypeSafeMatcher<Paper.Event> {
    private final int expectedEntityVersion;

    public static EventWithEntityVersionMatcher entityVersion(int expectedEntityVersion) {
        return new EventWithEntityVersionMatcher(expectedEntityVersion);
    }

    private EventWithEntityVersionMatcher(int expectedEntityVersion) {
        super(String.format("with entity version %s", expectedEntityVersion));
        this.expectedEntityVersion = expectedEntityVersion;
    }

    @Override
    protected boolean matchesSafely(Paper.Event item) {
        return expectedEntityVersion == item.getEntityVersion();
    }
}
