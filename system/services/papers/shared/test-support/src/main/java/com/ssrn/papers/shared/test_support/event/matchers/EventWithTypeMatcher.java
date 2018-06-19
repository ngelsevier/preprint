package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class EventWithTypeMatcher<TEvent extends Paper.Event> extends TypeSafeMatcher<Paper.Event> {
    private final Class<TEvent> expectedType;
    private final Matcher<TEvent> eventTypeSpecificMatcher;

    public static <T extends Paper.Event> Matcher<Paper.Event> type(Class<T> expectedType) {
        return new EventWithTypeMatcher<>(expectedType, null);
    }

    public static <T extends Paper.Event> Matcher<Paper.Event> type(Class<T> expectedType, Matcher<T> eventTypeSpecificMatcher) {
        return new EventWithTypeMatcher<>(expectedType, eventTypeSpecificMatcher);
    }

    private EventWithTypeMatcher(Class<TEvent> expectedType, Matcher<TEvent> eventTypeSpecificMatcher) {
        super();
        this.expectedType = expectedType;
        this.eventTypeSpecificMatcher = eventTypeSpecificMatcher;
    }

    @Override
    protected boolean matchesSafely(Paper.Event item) {
        return expectedType.equals(item.getClass()) &&
                (eventTypeSpecificMatcher == null || eventTypeSpecificMatcher.matches(item));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("with type '%s'", expectedType.getSimpleName()));

        if (eventTypeSpecificMatcher != null) {
            description.appendText(" and ");
            eventTypeSpecificMatcher.describeTo(description);
        }
    }
}
