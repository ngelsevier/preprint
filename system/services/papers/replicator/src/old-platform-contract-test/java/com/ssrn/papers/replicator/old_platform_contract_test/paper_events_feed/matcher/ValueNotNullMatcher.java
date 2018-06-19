package com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ValueNotNullMatcher {
    public static TypeSafeMatcher<Object> notNull() {
        return new TypeSafeMatcher<Object>()    {
            @Override
            public void describeTo(Description description) {
                description.appendText("Item should not be null");
            }

            @Override
            protected boolean matchesSafely(Object item) {
                return item != null;
            }
        };
    }
}
