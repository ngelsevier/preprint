package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

class UserHomePageView extends View {

    private Integer loggedInUser;

    protected UserHomePageView(Integer loggedInUser) {
        super("user-home-page.mustache");
        this.loggedInUser = loggedInUser;
    }


    public Integer getLoggedInUser() {
        return loggedInUser;
    }
}
