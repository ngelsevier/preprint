package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class SearchIndexPaperMatcher extends TypeSafeMatcher<SearchIndexPaper> {
    private final Matcher<SearchIndexPaper>[] matchers;

    @SafeVarargs
    public static Matcher<SearchIndexPaper> searchIndexPaperWith(Matcher<SearchIndexPaper>... matchers) {
        return new SearchIndexPaperMatcher(matchers);
    }

    private SearchIndexPaperMatcher(Matcher<SearchIndexPaper>... matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaper item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item));
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaper item, Description mismatchDescription) {
        List<Matcher<SearchIndexPaper>> failingMatchers = Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(item))
                .collect(Collectors.toList());

        int numberOfFailingMatchers = failingMatchers.size();

        mismatchDescription.appendText("search index paper had the following mismatches: [");

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
        description.appendList("a paper with ", " and ", "", asList(matchers));
    }
}
