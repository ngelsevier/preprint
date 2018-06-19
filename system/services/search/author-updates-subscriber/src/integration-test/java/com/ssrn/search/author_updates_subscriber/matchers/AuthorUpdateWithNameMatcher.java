package com.ssrn.search.author_updates_subscriber.matchers;

import com.ssrn.search.domain.AuthorUpdate;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorUpdateWithNameMatcher extends CustomTypeSafeMatcher<AuthorUpdate> {
    private final String expectedName;

    public static Matcher<AuthorUpdate> name(String expectedName) {
        return new AuthorUpdateWithNameMatcher(expectedName);
    }

    private AuthorUpdateWithNameMatcher(String expectedName) {
        super(String.format("name '%s'", expectedName));
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(AuthorUpdate item) {
        return expectedName.equals(item.getAuthor().getName());
    }
}
