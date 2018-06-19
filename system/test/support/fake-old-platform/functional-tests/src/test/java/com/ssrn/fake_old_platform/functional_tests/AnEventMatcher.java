package com.ssrn.fake_old_platform.functional_tests;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class AnEventMatcher extends TypeSafeMatcher<Event> {

    private final Matcher<Event>[] eventMatchers;

    @SafeVarargs
    public static AnEventMatcher anEvent(Matcher<Event>... eventMatchers) {
        return new AnEventMatcher(eventMatchers);
    }

    @SafeVarargs
    private AnEventMatcher(Matcher<Event>... eventMatchers) {
        this.eventMatchers = eventMatchers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("an event [", ", ", "]", asList(eventMatchers));
    }

    @Override
    protected boolean matchesSafely(Event actual) {
        return stream(eventMatchers).allMatch(matcher -> matcher.matches(actual));
    }
}
