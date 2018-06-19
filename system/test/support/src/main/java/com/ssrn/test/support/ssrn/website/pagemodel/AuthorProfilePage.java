package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

import static com.ssrn.test.support.http.HttpClient.queryParameter;

public class AuthorProfilePage extends WebPageBase<AuthorProfilePage.Visit> {

    AuthorProfilePage(String baseUrl, String authorId) {
        super(baseUrl, String.format("/author=%s", authorId));
    }

    @Override
    protected AuthorProfilePage.Visit createVisit(Browser browser) {
        return new Visit(browser);
    }

    public static class Visit {
        private final Browser browser;

        private Visit(Browser browser) {
            this.browser = browser;
        }

        public String authorId() {
            return browser.getTextContentOfEachElementAt("div.info-col h1").get(0);
        }

        public String authorName() {
            return browser.getTextContentOfEachElementAt("div.info-col h1").get(0);
        }

    }
}
