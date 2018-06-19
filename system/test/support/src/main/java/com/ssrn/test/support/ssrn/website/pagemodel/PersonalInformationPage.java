package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

import java.util.AbstractMap;
import java.util.ArrayList;

public class PersonalInformationPage extends WebPageBase<PersonalInformationPage.Visit> {
    PersonalInformationPage(String baseUrl, String partId) {
        super(baseUrl, "/Participant.cfm",
                new AbstractMap.SimpleEntry<>("rectype",
                        new ArrayList<Object>() {{
                            add("edit");
                        }}
                ),
                new AbstractMap.SimpleEntry<>("partid",
                        new ArrayList<Object>() {{
                            add(partId);
                        }}
                )
        );
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser);
    }

    public static class Visit {
        private final Browser browser;

        public Visit(Browser browser) {
            this.browser = browser;
        }

        public Visit enterPublicDisplayNameTo(String firstName, String lastName) {
            browser.enterTextInField("input[name=txtPrefFirstName]", firstName);
            browser.enterTextInField("input[name=txtPrefLastName]", lastName);
            return this;
        }

        public Visit submitUpdates() {
            browser.clickElement("#btnSubmit");
            return this;
        }

        public String getAuthorId() {
            return browser.valueOfCurrentLocationQueryParameter("partID");
        }
    }
}
