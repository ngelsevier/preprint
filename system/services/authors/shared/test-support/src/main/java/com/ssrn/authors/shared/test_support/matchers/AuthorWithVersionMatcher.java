package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Author;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorWithVersionMatcher extends CustomTypeSafeMatcher<Author> {
    private final int expectedVersion;

    public static Matcher version(int expectedVersion) {
        return new AuthorWithVersionMatcher(expectedVersion);
    }

    private AuthorWithVersionMatcher(int expectedVersion) {
        super(String.format("version '%d'", expectedVersion));
        this.expectedVersion = expectedVersion;
    }

    @Override
    protected boolean matchesSafely(Author item) {
        return expectedVersion == item.getVersion();
    }
}
