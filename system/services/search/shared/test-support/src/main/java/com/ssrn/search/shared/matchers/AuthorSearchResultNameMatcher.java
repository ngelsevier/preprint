package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.AuthorSearchResult;
import com.ssrn.search.shared.BaseSearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorSearchResultNameMatcher extends CustomTypeSafeMatcher<BaseSearchResult> {
    private final String expectedName;

    public static Matcher<BaseSearchResult> name(String expectedName) {
        return new AuthorSearchResultNameMatcher(expectedName);
    }

    private AuthorSearchResultNameMatcher(String expectedName) {
        super(String.format("with name '%s'", expectedName));
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(BaseSearchResult item) {
        if (expectedName == null){
            return ((AuthorSearchResult)item).getName() == null;
        }
        return expectedName.equals(((AuthorSearchResult)item).getName());
    }
}
