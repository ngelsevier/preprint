package com.ssrn.test.support.ssrn.website.pagemodel;

public class ProfileDropDownMenu {

    private static final String OPEN_LINK_CSS_SELECTOR = "#profile";

    private final PersonalInformationPage personalInformationPage;
    private final int pageLoadTimeoutSeconds;

    ProfileDropDownMenu(PersonalInformationPage personalInformationPage, int pageLoadTimeoutSeconds) {
        this.personalInformationPage = personalInformationPage;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    public Hyperlink<PersonalInformationPage.Visit> personalInfoLink() {
        return new PopUpDialogHyperlink<>(OPEN_LINK_CSS_SELECTOR, "#personal", personalInformationPage, pageLoadTimeoutSeconds);
    }

}
