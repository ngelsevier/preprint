package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class StageOneClassificationSearchPageView extends View{

    private final String abstractId;

    protected StageOneClassificationSearchPageView(String abstractId) {
        super("stage-one-classification-search-page.mustache");
        this.abstractId = abstractId;
    }

    public String getAbstractId() {
        return abstractId;
    }
}
