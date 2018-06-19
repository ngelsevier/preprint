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

public class SearchResultAuthorMatcher extends TypeSafeMatcher<SearchResult.Author> {
    private Matcher<SearchResult.Author>[] matchers;

    public static Matcher<SearchResult.Author> anAuthor(Matcher<SearchResult.Author>... matchers) {
        return new SearchResultAuthorMatcher(matchers);
    }

    private SearchResultAuthorMatcher(Matcher<SearchResult.Author>[] authorMatchers) {
        this.matchers = authorMatchers;
    }

    @Override
    protected boolean matchesSafely(SearchResult.Author item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item));
    }

    @Override
    protected void describeMismatchSafely(SearchResult.Author item, Description mismatchDescription) {
        mismatchDescription.appendText("author had the following mismatches: ");

        List<Matcher<SearchResult.Author>> failingMatchers = Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(item))
                .collect(Collectors.toList());

        int numberOfFailingMatchers = failingMatchers.size();

        IntStream.range(0, numberOfFailingMatchers)
                .forEach(i -> {
                    failingMatchers.get(i).describeMismatch(item, mismatchDescription);

                    if (numberOfFailingMatchers > 1 && i < numberOfFailingMatchers - 1) {
                        mismatchDescription.appendText(", ");
                    }
                });
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("an author with ", " and ", "", asList(matchers));
    }
}