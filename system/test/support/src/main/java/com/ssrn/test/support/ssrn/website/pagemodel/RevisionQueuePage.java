package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class RevisionQueuePage  extends WebPageBase<RevisionQueuePage.Visit> {

    public RevisionQueuePage(String baseUrl) {
        super(baseUrl, "/submissions/Revisequeue.cfm");
    }

    @Override
    protected RevisionQueuePage.Visit createVisit(Browser browser) {
        return new Visit(browser);
    }

    public class Visit {
        private static final int TIME_OUT_IN_SECONDS = 60;
        private final Browser browser;

        public Visit(Browser browser) {

            this.browser = browser;
        }

        public void startRevisionReview(String revisionAbstractId) {
            String abstractToModify = String.format("input[onclick*='%s']", revisionAbstractId);
            browser.waitUntilDisplayingElementAt(abstractToModify, TIME_OUT_IN_SECONDS);
            browser.clickElement(abstractToModify);
        }
    }
}
