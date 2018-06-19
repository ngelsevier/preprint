package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class ModifySubmissionPopup extends WebPageBase<ModifySubmissionPopup.Visit> {

    private int pageLoadTimeoutSeconds;

    ModifySubmissionPopup(String baseUrl, int pageLoadTimeoutSeconds) {
        super(baseUrl, "/submissions/ProcessRemove.cfm");
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser, pageLoadTimeoutSeconds);
    }

    public static class Visit {
        private final Browser browser;
        private int timeoutInSeconds;

        private Visit(Browser browser, int pageLoadTimeoutSeconds) {
            this.browser = browser;
            timeoutInSeconds = pageLoadTimeoutSeconds;
        }

        public void deactivatePaper() {
            String cssSelector = "input[type='radio'][value='Remove']";
            browser.waitUntilDisplayingElementAt(cssSelector, timeoutInSeconds);
            browser.clickElement(cssSelector);
            browser.clickElement("input[type='button'][value='Continue']");
        }

    }
}
