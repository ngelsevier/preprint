package com.ssrn.search.author_updates_subscriber.matchers;

import com.ssrn.search.domain.AuthorUpdate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static java.util.Arrays.asList;

public class AuthorUpdateMatcher extends TypeSafeMatcher<AuthorUpdate> {
    private final Matcher[] matchers;

    public static Matcher<AuthorUpdate> anAuthorUpdateWith(Matcher... matchers) {
        return new AuthorUpdateMatcher(matchers);
    }

    AuthorUpdateMatcher(Matcher... matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(AuthorUpdate item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item));
    }

    @Override
    protected void describeMismatchSafely(AuthorUpdate item, Description mismatchDescription) {
        Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(item))
                .forEach(matcher -> matcher.describeMismatch(item, mismatchDescription));
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("An Author update with ", " and ", "", asList(matchers));
    }
}
