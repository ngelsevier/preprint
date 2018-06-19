package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RevisionDetailPage extends WebPageBase<RevisionDetailPage.Visit> {
    public RevisionDetailPage(String baseUrl) {
        super(baseUrl, "/submissions/ReviseDetail.cfm");
    }

    @Override
    protected RevisionDetailPage.Visit createVisit(Browser browser) {
        return new RevisionDetailPage.Visit(browser);
    }

    public class Visit {
        private static final int TIME_OUT_IN_SECONDS = 60;
        private final Browser browser;

        public Visit(Browser browser) {

            this.browser = browser;
        }

        public void approveMinorRevision(String revisionAbstractId) {
            browser.inFrameWithName("treeFrame", iframeId -> {
                browser.waitUntilDisplayingElementAt(String.format("a[href*='abstract_id'][href*='%s']", revisionAbstractId), TIME_OUT_IN_SECONDS);

                //select a network
                browser.clickElement("input[name='2990376']");

                //wait for the selection popup window to close
                sleepFor(1, SECONDS);

                String revisionToModify = "input[name='cmdApproveMin']";
                browser.waitUntilDisplayingElementAt(revisionToModify, TIME_OUT_IN_SECONDS);
                browser.clickElement(revisionToModify);

            }, TIME_OUT_IN_SECONDS);
        }
    }
}
