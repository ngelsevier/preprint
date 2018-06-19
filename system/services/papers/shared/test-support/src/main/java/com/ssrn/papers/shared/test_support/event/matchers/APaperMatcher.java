package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class APaperMatcher extends TypeSafeMatcher<Paper> {

    private final Matcher<Paper>[] paperMatchers;

    @SafeVarargs
    public static APaperMatcher aPaperWith(Matcher<Paper>... paperMatchers) {
        return new APaperMatcher(paperMatchers);
    }

    @SafeVarargs
    private APaperMatcher(Matcher<Paper>... paperMatchers) {
        this.paperMatchers = paperMatchers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("a paper [", ", ", "]", asList(paperMatchers));
    }

    @Override
    protected boolean matchesSafely(Paper actual) {
        return stream(paperMatchers).allMatch(matcher -> matcher.matches(actual));
    }

    @Override
    protected void describeMismatchSafely(Paper actual, Description mismatchDescription) {
        Arrays.stream(paperMatchers)
                .filter(matcher -> !matcher.matches(actual))
                .forEach(matcher -> matcher.describeMismatch(actual, mismatchDescription));
    }
}
