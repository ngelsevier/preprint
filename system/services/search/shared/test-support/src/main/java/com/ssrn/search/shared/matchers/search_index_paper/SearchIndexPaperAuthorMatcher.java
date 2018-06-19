package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaperAuthor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class SearchIndexPaperAuthorMatcher extends TypeSafeMatcher<SearchIndexPaperAuthor> {
    private final Matcher<SearchIndexPaperAuthor>[] matchers;

    @SafeVarargs
    public static Matcher<SearchIndexPaperAuthor> authorWith(Matcher<SearchIndexPaperAuthor>... matchers) {
        return new SearchIndexPaperAuthorMatcher(matchers);
    }

    @SafeVarargs
    private SearchIndexPaperAuthorMatcher(Matcher<SearchIndexPaperAuthor>... matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaperAuthor item) {
        return Arrays.stream(matchers).allMatch(matcher -> matcher.matches(item));
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaperAuthor item, Description mismatchDescription) {
        mismatchDescription.appendText("author had the following mismatches: ");

        List<Matcher<SearchIndexPaperAuthor>> failingMatchers = Arrays.stream(matchers)
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
