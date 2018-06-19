package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class PaperRevisionAcceptedPageView extends View{
    private final String abstractId;

    public PaperRevisionAcceptedPageView(String abstractId) {
        super("paper-revision-accepted-page.mustache");
        this.abstractId = abstractId;
    }

    public String getAbstractId() {
        return abstractId;
    }
}
