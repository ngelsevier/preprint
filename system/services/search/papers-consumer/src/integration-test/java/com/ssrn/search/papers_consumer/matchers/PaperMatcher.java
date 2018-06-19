package com.ssrn.search.papers_consumer.matchers;

import com.ssrn.search.domain.Paper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static java.util.Arrays.asList;

public class PaperMatcher extends TypeSafeMatcher<Paper> {
    private final Matcher[] matchers;

    public static Matcher<Paper> aPaperWith(Matcher... matchers) {
        return new PaperMatcher(matchers);
    }

    PaperMatcher(Matcher... matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(Paper item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item));
    }

    @Override
    protected void describeMismatchSafely(Paper item, Description mismatchDescription) {
        Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(item))
                .forEach(matcher -> matcher.describeMismatch(item, mismatchDescription));
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("A paper with ", " and ", "", asList(matchers));
    }
}
