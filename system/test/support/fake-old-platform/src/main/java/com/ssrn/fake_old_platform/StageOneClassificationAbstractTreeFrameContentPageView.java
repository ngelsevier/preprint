package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class StageOneClassificationAbstractTreeFrameContentPageView extends View {

    private final String abstractId;
    private final boolean consideredIrrelevant;

    protected StageOneClassificationAbstractTreeFrameContentPageView(String abstractId, boolean consideredIrrelevant) {
        super("stage-one-classification-abstract-page-treeframe-content.mustache");
        this.abstractId = abstractId;

        this.consideredIrrelevant = consideredIrrelevant;
    }

    public String getAbstractId() {
        return abstractId;
    }

    public String isConsideredIrrelevant() {
        return consideredIrrelevant ? "checked" : "";
    }
}
