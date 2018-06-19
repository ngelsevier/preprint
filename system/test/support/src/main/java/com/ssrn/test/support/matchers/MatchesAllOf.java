package com.ssrn.test.support.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Matcher derived from official hamcrest matchers with improved mismatch description
 */
public class MatchesAllOf<T> extends AllOf<T> {
    private final Matcher<T>[] matchers;

    public static <T> Matcher<T> matchesAllOf(Matcher<T> firstMatcher, List<Matcher<T>> additionalMatchers) {
        ArrayList<Matcher<T>> matchers = new ArrayList<>();
        matchers.add(firstMatcher);
        matchers.addAll(additionalMatchers);

        return matchesAllOf(matchers);
    }

    public static <T> Matcher<T> matchesAllOf(Matcher<T> firstMatcher, Matcher<T> secondMatcher, List<Matcher<T>> additionalMatchers) {
        ArrayList<Matcher<T>> matchers = new ArrayList<>();
        matchers.add(firstMatcher);
        matchers.add(secondMatcher);
        matchers.addAll(additionalMatchers);

        return matchesAllOf(matchers);
    }

    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> matchesAllOf(List<Matcher<T>> matchers) {
        return new MatchesAllOf<>(matchers.toArray(new Matcher[0]));
    }

    @SafeVarargs
    public static <T> Matcher<T> matchesAllOf(Matcher<T>... matchers) {
        return new MatchesAllOf<>(matchers);
    }

    private MatchesAllOf(Matcher<T>[] matchers) {
        super(asList(matchers));
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Object o, Description mismatch) {
        for (Matcher<? super T> matcher : matchers) {
            if (!matcher.matches(o)) {
                mismatch.appendText("we were unable to match [");
                mismatch.appendDescriptionOf(matcher).appendText("] ");
                mismatch.appendText("\nwhich failed with mismatch description: ");
                matcher.describeMismatch(o, mismatch);
                return false;
            }
        }
        return true;

    }
}
