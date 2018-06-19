package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Author;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static java.util.Arrays.asList;

public class AuthorMatcher extends TypeSafeMatcher<Author> {
    private final Matcher[] matchers;

    public static Matcher<Author> anAuthorWith(Matcher... matchers) {
        return new AuthorMatcher(matchers);
    }

    AuthorMatcher(Matcher... matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(Author item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item));
    }

    @Override
    protected void describeMismatchSafely(Author item, Description mismatchDescription) {
        Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(item))
                .forEach(matcher -> matcher.describeMismatch(item, mismatchDescription));
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("An author with ", " and ", "", asList(matchers));
    }
}
