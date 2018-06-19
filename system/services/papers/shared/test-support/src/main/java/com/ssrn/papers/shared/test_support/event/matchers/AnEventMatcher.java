package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class AnEventMatcher extends TypeSafeMatcher<Paper.Event> {

    private final Matcher<Paper.Event>[] eventMatchers;

    @SafeVarargs
    public static AnEventMatcher anEventWith(Matcher<Paper.Event>... eventMatchers) {
        return new AnEventMatcher(eventMatchers);
    }

    @SafeVarargs
    private AnEventMatcher(Matcher<Paper.Event>... eventMatchers) {
        this.eventMatchers = eventMatchers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("an event [", ", ", "]", asList(eventMatchers));
    }

    @Override
    protected boolean matchesSafely(Paper.Event actual) {
        return stream(eventMatchers).allMatch(matcher -> matcher.matches(actual));
    }
}
