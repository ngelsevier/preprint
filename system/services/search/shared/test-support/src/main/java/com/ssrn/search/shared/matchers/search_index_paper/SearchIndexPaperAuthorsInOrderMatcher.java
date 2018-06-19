package com.ssrn.search.shared.matchers.search_index_paper;

import com.ssrn.search.shared.SearchIndexPaper;
import com.ssrn.search.shared.SearchIndexPaperAuthor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class SearchIndexPaperAuthorsInOrderMatcher extends TypeSafeMatcher<SearchIndexPaper> {
    private final Matcher<SearchIndexPaperAuthor>[] matchers;

    @SafeVarargs
    public static Matcher<SearchIndexPaper> authorsInOrder(Matcher<SearchIndexPaperAuthor>... matchers) {
        return new SearchIndexPaperAuthorsInOrderMatcher(matchers);
    }

    private SearchIndexPaperAuthorsInOrderMatcher(Matcher<SearchIndexPaperAuthor>[] matchers) {
        this.matchers = matchers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("authors in order [(", ") and (", ")]", asList(matchers));
    }

    @Override
    protected void describeMismatchSafely(SearchIndexPaper item, Description mismatchDescription) {
        SearchIndexPaperAuthor[] authors = item.getAuthors();

        int authorCount = authors.length;
        int matcherCount = matchers.length;

        if (authorCount != matcherCount) {
            mismatchDescription.appendText(String.format("found %d authors when %d were expected", authorCount, matcherCount));
            return;
        }
        List<Matcher<SearchIndexPaperAuthor>> failingMatchers = IntStream.range(0, authorCount)
                .filter(i -> !matchers[i].matches(authors[i]))
                .mapToObj(i -> matchers[i])
                .collect(Collectors.toList());

        int numberOfFailingMatchers = failingMatchers.size();

        mismatchDescription.appendText("authors had the following mismatches: [");

        IntStream.range(0, numberOfFailingMatchers)
                .forEach(i -> {
                    mismatchDescription.appendText("(");
                    mismatchDescription.appendText(String.format("author at index %d failed to match: ", i));
                    failingMatchers.get(i).describeMismatch(authors[i], mismatchDescription);
                    mismatchDescription.appendText(")");

                    if (numberOfFailingMatchers > 1 && i < numberOfFailingMatchers - 1) {
                        mismatchDescription.appendText(", ");
                    }
                });

        mismatchDescription.appendText("]");
    }

    @Override
    protected boolean matchesSafely(SearchIndexPaper item) {
        SearchIndexPaperAuthor[] authors = item.getAuthors();

        int authorCount = authors.length;

        return authorCount == matchers.length &&
                IntStream.range(0, authorCount).allMatch(i -> matchers[i].matches(authors[i]));

    }
}
