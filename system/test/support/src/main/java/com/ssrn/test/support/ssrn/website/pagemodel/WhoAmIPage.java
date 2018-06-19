package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class WhoAmIPage extends WebPageBase<WhoAmIPage.Visit> {
    private final int pageLoadTimeoutSeconds;

    public WhoAmIPage(String baseUrl, int pageLoadTimeoutSeconds) {
        super(baseUrl, "/rest/user/whoami");
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser, pageLoadTimeoutSeconds);
    }

    public static class Visit {

        public String visit() {
            return browser.getLoadedPageSource();
        }

        private final Browser browser;
        private final int pageLoadTimeoutSeconds;

        public Visit(Browser browser, int pageLoadTimeoutSeconds) {
            this.browser = browser;
            this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
        }
    }
}
