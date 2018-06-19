package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

class ModifySubmissionPopupView extends View{

    private int abstractId;

    ModifySubmissionPopupView(int abstractId) {
        super("modify-submission-popup.mustache");
        this.abstractId = abstractId;
    }

    public int getAbstractId() {
        return abstractId;
    }
}
