package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class ArticlePageView extends View {

    private final String title;

    protected ArticlePageView(String title) {
        super("article-page.mustache");
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
