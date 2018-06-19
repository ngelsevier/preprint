package com.ssrn.fake_old_platform.functional_tests;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Arrays.asList;

public class EventWithJsonDataMatcher extends TypeSafeMatcher<Event> {
    private final Matcher<? super Object> jsonMatcher;

    public static Matcher<Event> withDataSatisfying(Matcher<? super Object> jsonMatchers) {
        return new EventWithJsonDataMatcher(jsonMatchers);
    }

    private EventWithJsonDataMatcher(Matcher<? super Object> jsonMatcher) {
        this.jsonMatcher = jsonMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("with json data satisfying [", ",", "]", asList(jsonMatcher));
    }

    @Override
    protected boolean matchesSafely(Event item) {
        return jsonMatcher.matches(item.getDataJson());
    }
}
