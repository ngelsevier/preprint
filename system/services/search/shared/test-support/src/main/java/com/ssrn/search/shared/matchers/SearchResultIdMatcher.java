package com.ssrn.search.shared.matchers;

import com.ssrn.search.shared.BaseSearchResult;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class SearchResultIdMatcher extends CustomTypeSafeMatcher<BaseSearchResult> {
    private final String expectedId;

    public static Matcher<BaseSearchResult> id(String expectedId) {
        return new SearchResultIdMatcher(expectedId);
    }

    private SearchResultIdMatcher(String expectedId) {
        super(String.format("with id '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(BaseSearchResult item) {
        return expectedId.equals(item.getId());
    }
}
