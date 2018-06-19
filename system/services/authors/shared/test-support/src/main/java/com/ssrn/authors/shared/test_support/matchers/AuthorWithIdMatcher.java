package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Author;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorWithIdMatcher extends CustomTypeSafeMatcher<Author> {
    private final String expectedId;

    public static Matcher id(String expectedId) {
        return new AuthorWithIdMatcher(expectedId);
    }

    private AuthorWithIdMatcher(String expectedId) {
        super(String.format("ID '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(Author item) {
        return expectedId.equals(item.getId());
    }
}
