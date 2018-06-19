package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class MyPapersPage extends WebPageBase<MyPapersPage.Visit> {
    private final SsrnWebsite ssrnWebsite;
    private final int pageLoadTimeout;

    public MyPapersPage(String baseUrl, String relativePath, SsrnWebsite ssrnWebsite, int pageLoadTimeout) {
        super(baseUrl, relativePath);
        this.ssrnWebsite = ssrnWebsite;
        this.pageLoadTimeout = pageLoadTimeout;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(ssrnWebsite, pageLoadTimeout, browser);
    }

    public static class Visit {

        private final SsrnWebsite ssrnWebsite;
        private final int pageLoadTimeoutSeconds;
        private final Browser browser;

        public Visit(SsrnWebsite ssrnWebsite, int pageLoadTimeoutSeconds, Browser browser) {
            this.ssrnWebsite = ssrnWebsite;
            this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
            this.browser = browser;
        }

        public Hyperlink<PaperSubmissionPage.Visit> editButtonForAbstract(String abstractId) {
            return new CssSelectorLocatedHyperlink<>(String.format("#dEdit_%s a", abstractId), ssrnWebsite.paperSubmissionPage(), pageLoadTimeoutSeconds);
        }

        public CssSelectorLocatedPopupHyperlink<ModifySubmissionPopup.Visit> modify(String abstractId) {
            return new CssSelectorLocatedPopupHyperlink<>(String.format("a[onclick*='%s'] img[src='Images/Modify.gif']", abstractId), ssrnWebsite.modifySubmissionPopup(), pageLoadTimeoutSeconds);
        }

        public void createRevision(String abstractId) {
            String reviseButtonCssSelector = String.format("#dEdit_%s img", abstractId);
            browser.waitUntilDisplayingElementAt(reviseButtonCssSelector, pageLoadTimeoutSeconds);
            browser.clickElement(reviseButtonCssSelector);
        }
    }
}
