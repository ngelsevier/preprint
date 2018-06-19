package com.ssrn.test.support.ssrn.website.pagemodel;

public class NavigationBar {
    private final SsrnWebsite ssrnWebsite;
    private final int pageLoadTimeoutSeconds;

    NavigationBar(SsrnWebsite ssrnWebsite, int pageLoadTimeoutSeconds) {
        this.ssrnWebsite = ssrnWebsite;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    public Hyperlink<PaperSubmissionPage.Visit> submitAPaperLink() {
        return new CssSelectorLocatedHyperlink<>("#headerId #submitPaperLink", ssrnWebsite.paperSubmissionPage(), pageLoadTimeoutSeconds);
    }

    public ProfileDropDownMenu profileDropDown(String partId) {
        return new ProfileDropDownMenu(ssrnWebsite.personalInformationPage(partId), pageLoadTimeoutSeconds);
    }

}
