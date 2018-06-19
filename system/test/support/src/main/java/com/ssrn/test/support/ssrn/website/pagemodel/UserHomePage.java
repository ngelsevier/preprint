package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class UserHomePage extends WebPageBase<UserHomePage.Visit> {

    private final SsrnWebsite ssrnWebsite;

    UserHomePage(String baseUrl, SsrnWebsite ssrnWebsite) {
        super(baseUrl, "/UserHome.cfm");
        this.ssrnWebsite = ssrnWebsite;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit();
    }

    public class Visit {
        public SideBar sideBar() {
            return ssrnWebsite.sideBar();
        }

    }
}