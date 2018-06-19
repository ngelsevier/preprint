package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class SubmissionConfirmationView extends View{

    private final String ssrnAccountId;

    protected SubmissionConfirmationView(String ssrnAccountId) {
        super("submission-confirmation-page.mustache");
        this.ssrnAccountId = ssrnAccountId;
    }

    public String getSsrnAccountId() {
        return ssrnAccountId;
    }
}
