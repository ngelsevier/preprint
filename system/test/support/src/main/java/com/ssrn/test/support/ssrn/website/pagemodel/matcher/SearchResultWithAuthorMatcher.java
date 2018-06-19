package com.ssrn.test.support.ssrn.website.pagemodel.matcher;

import com.ssrn.test.support.ssrn.website.pagemodel.SearchResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class SearchResultWithAuthorMatcher extends TypeSafeMatcher<SearchResult> {
    private final Matcher<SearchResult.Author>[] matchers;

    public static Matcher<SearchResult> anAuthorSearchResultWith(Matcher<SearchResult.Author> matchers) {
        return new SearchResultWithAuthorMatcher(matchers);
    }

    private SearchResultWithAuthorMatcher(Matcher<SearchResult.Author>... matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item.getAuthor()));
    }

    @Override
    protected void describeMismatchSafely(SearchResult item, Description mismatchDescription) {
        List<Matcher<SearchResult.Author>> failingMatchers = Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(item))
                .collect(Collectors.toList());

        int numberOfFailingMatchers = failingMatchers.size();

        mismatchDescription.appendText("search result had the following mismatches: [");

        IntStream.range(0, numberOfFailingMatchers)
                .forEach(i -> {
                    mismatchDescription.appendText("(");
                    failingMatchers.get(i).describeMismatch(item, mismatchDescription);
                    mismatchDescription.appendText(")");

                    if (numberOfFailingMatchers > 1 && i < numberOfFailingMatchers - 1) {
                        mismatchDescription.appendText(", ");
                    }
                });

        mismatchDescription.appendText("]");
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("a search result with ", " and ", "", asList(matchers));
    }
}
