package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class AuthorProfilePageView extends View {

    private final String authorName;

    public AuthorProfilePageView(String authorName) {
        super("author-profile-page.mustache");
        this.authorName = authorName;
    }

    public String getAuthorName() {
        return authorName;
    }
}
