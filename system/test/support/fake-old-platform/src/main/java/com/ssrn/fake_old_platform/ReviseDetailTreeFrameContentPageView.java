package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class ReviseDetailTreeFrameContentPageView  extends View{
    private final int abstractId;

    protected ReviseDetailTreeFrameContentPageView(int abstractId) {
        super("revise-detail-tree-frame-content.mustache");
        this.abstractId = abstractId;
    }

    public int getAbstractId() {
        return abstractId;
    }
}
