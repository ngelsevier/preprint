package com.ssrn.search.author_updates_subscriber.matchers;

import com.ssrn.search.domain.AuthorUpdate;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorUpdateWithIdMatcher extends CustomTypeSafeMatcher<AuthorUpdate> {
    private final String expectedId;

    public static Matcher id(String expectedId) {
        return new AuthorUpdateWithIdMatcher(expectedId);
    }

    private AuthorUpdateWithIdMatcher(String expectedId) {
        super(String.format("ID '%s'", expectedId));
        this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(AuthorUpdate item) {
        return expectedId.equals(item.getId());
    }
}
