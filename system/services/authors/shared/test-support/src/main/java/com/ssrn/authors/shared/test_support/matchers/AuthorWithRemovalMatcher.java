package com.ssrn.authors.shared.test_support.matchers;

import com.ssrn.authors.domain.Author;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorWithRemovalMatcher extends CustomTypeSafeMatcher<Author> {
    private final boolean expectedRemoval;

    public static Matcher<Author> removed(boolean expectRemoval) {
        return new AuthorWithRemovalMatcher(expectRemoval);
    }

    private AuthorWithRemovalMatcher(boolean expectRemoval) {
        super(String.format("removed flag is '%s'", expectRemoval));
        this.expectedRemoval = expectRemoval;
    }

    @Override
    protected boolean matchesSafely(Author item) {
        return expectedRemoval == item.isRemoved();
    }
}
