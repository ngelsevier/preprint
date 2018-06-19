package com.ssrn.fake_old_platform.functional_tests;

import org.hamcrest.CustomTypeSafeMatcher;

public class EventWithEntityVersionMatcher extends CustomTypeSafeMatcher<Event> {
    private final int expectedEntityVersion;

    public static EventWithEntityVersionMatcher withEntityVersion(int expectedEntityVersion) {
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
