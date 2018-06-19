package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class LoginPage extends WebPageBase<LoginPage.Visit> {

    private final UserHomePage userHomePage;
    private final int pageLoadTimeoutSeconds;

    public LoginPage(String baseUrl, UserHomePage userHomePage, int pageLoadTimeoutSeconds) {
        super(baseUrl, "/login/pubSignInJoin.cfm");
        this.userHomePage = userHomePage;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser, userHomePage, pageLoadTimeoutSeconds);
    }

    public static class Visit {
        private final Browser browser;
        private final UserHomePage userHomePage;
        private final int pageLoadTimeoutSeconds;

        private Visit(Browser browser, UserHomePage userHomePage, int pageLoadTimeoutSeconds) {
            this.browser = browser;
            this.userHomePage = userHomePage;
            this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
        }

        public UserHomePage.Visit logInAs(String userName, String password) {
            browser.enterTextInField(".input-email", userName);
            browser.enterTextInField(".input-pass", password);
            browser.clickElement("#signinBtn");
            return userHomePage.loadedIn(browser, pageLoadTimeoutSeconds);
        }
    }
}
