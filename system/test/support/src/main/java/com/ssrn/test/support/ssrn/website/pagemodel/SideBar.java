package com.ssrn.test.support.ssrn.website.pagemodel;

public class SideBar {
    private final SsrnWebsite ssrnWebsite;
    private int pageLoadTimeoutSeconds;

    SideBar(SsrnWebsite ssrnWebsite, int pageLoadTimeoutSeconds) {
        this.ssrnWebsite = ssrnWebsite;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    public DisplayedTextLocatedHyperlink<MyPapersPage.Visit> myPapersLink() {
        return new DisplayedTextLocatedHyperlink<>("My Papers", ".leftmenuTD", ssrnWebsite.myPapersPage(), pageLoadTimeoutSeconds);
    }

    public CssSelectorLocatedHyperlink<StageOneClassificationPage.Visit> jensenStageOneQueueLink() {
        return new CssSelectorLocatedHyperlink<>(".jensen-menu a[href$='Stage1queue.cfm']", ssrnWebsite.stageOneClassificationPage(), pageLoadTimeoutSeconds);
    }

    public Hyperlink<RevisionQueuePage.Visit> jensenRevisionQueueLink() {
        return new CssSelectorLocatedHyperlink<>(".jensen-menu a[href$='Revisequeue.cfm']", ssrnWebsite.revisionQueuePage(), pageLoadTimeoutSeconds);
    }
}
