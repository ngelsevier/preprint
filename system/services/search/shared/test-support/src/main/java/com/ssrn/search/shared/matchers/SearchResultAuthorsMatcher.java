package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.BaseSearchResult;
import com.ssrn.search.shared.PaperSearchResult;
import com.ssrn.search.shared.SearchResultAuthor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SearchResultAuthorsMatcher extends TypeSafeMatcher<BaseSearchResult> {
    private final Matcher<SearchResultAuthor[]> matcher;

    public static Matcher<BaseSearchResult> paperAuthors(Matcher<SearchResultAuthor[]> matcher) {
        return new SearchResultAuthorsMatcher(matcher);
    }

    private SearchResultAuthorsMatcher(Matcher<SearchResultAuthor[]> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with authors ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected boolean matchesSafely(BaseSearchResult item) {
        return matcher.matches(((PaperSearchResult)item).getAuthors());
    }
}
