package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Author;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorWithNameMatcher extends CustomTypeSafeMatcher<Author> {
    private final String expectedName;

    public static Matcher<Author> name(String expectedName) {
        return new AuthorWithNameMatcher(expectedName);
    }

    private AuthorWithNameMatcher(String expectedName) {
        super(String.format("name '%s'", expectedName));
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(Author item) {
        return expectedName.equals(item.getName());
    }
}
