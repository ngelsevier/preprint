package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class PaperRevisionAcceptedPage extends WebPageBase<PaperRevisionAcceptedPage.Visit> {
    public PaperRevisionAcceptedPage(String baseUrl) {
        super(baseUrl, "/submissions/ReviseDB.cfm");
    }

    @Override
    protected PaperRevisionAcceptedPage.Visit createVisit(Browser browser) {
        return new PaperRevisionAcceptedPage.Visit(browser);
    }

    public class Visit {
        private static final int TIME_OUT_IN_SECONDS = 60;
        private final Browser browser;

        public Visit(Browser browser) {

            this.browser = browser;
        }

        public void approve(String revisionAbstractId) {
            browser.waitUntilDisplayingElementAt(String.format("form[name='form1'][action*='%s']", revisionAbstractId), TIME_OUT_IN_SECONDS);

            browser.clickElement("input[name='cmdApprove']");
        }
    }
}
