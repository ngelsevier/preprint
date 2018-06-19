package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

class PersonalInformationPageView extends View {
    private Integer partId;

    PersonalInformationPageView(Integer partId) {
        super("personal-information-page.mustache");
        this.partId = partId;
    }


    public Integer getPartId() {
        return partId;
    }
}
