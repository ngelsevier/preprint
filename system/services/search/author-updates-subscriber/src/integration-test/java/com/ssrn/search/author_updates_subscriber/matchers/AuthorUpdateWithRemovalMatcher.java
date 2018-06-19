package com.ssrn.search.author_updates_subscriber.matchers;

import com.ssrn.search.domain.AuthorUpdate;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class AuthorUpdateWithRemovalMatcher extends CustomTypeSafeMatcher<AuthorUpdate> {
    private final boolean expectedRemoval;

    public static Matcher<AuthorUpdate> removed(boolean expectRemoval) {
        return new AuthorUpdateWithRemovalMatcher(expectRemoval);
    }

    private AuthorUpdateWithRemovalMatcher(boolean expectedRemoval) {
        super(String.format("Removal '%s'", expectedRemoval));
        this.expectedRemoval = expectedRemoval;
    }

    @Override
    protected boolean matchesSafely(AuthorUpdate item) {
        return expectedRemoval == item.getAuthor().isRemoved();
    }
}
