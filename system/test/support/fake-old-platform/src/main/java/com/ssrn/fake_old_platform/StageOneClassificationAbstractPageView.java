package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class StageOneClassificationAbstractPageView extends View {

    private final String abstractId;

    protected StageOneClassificationAbstractPageView(String abstractId) {
        super("stage-one-classification-abstract-page.mustache");
        this.abstractId = abstractId;
    }

    public String getAbstractId() {
        return abstractId;
    }
}
