package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

public class SimpleSubmissionPageView extends View {

    private int abstractId;
    private final int revisionAbstractId;
    private String ssrnAccountId;

    protected SimpleSubmissionPageView(int abstractId, int revisionAbstractId, String ssrnAccountId) {
        super("simple-submission-page.mustache");
        this.abstractId = abstractId;
        this.revisionAbstractId = revisionAbstractId;
        this.ssrnAccountId = ssrnAccountId;
    }

    public int getAbstractId() {
        return isRevision() ? revisionAbstractId : abstractId;
    }

    public String getSsrnAccountId() {
        return ssrnAccountId;
    }

    public boolean isRevision() {
        return revisionAbstractId != 0;
    }
}
