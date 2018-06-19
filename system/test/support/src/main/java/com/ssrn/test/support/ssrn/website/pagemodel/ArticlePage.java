package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class ArticlePage extends WebPageBase<ArticlePage.Visit>{

    public ArticlePage(String baseUrl, String abstractId) {
        super(baseUrl, String.format("/abstract=%s", abstractId));
    }

    public static String extractAbstractIdFromArticlePageUrl(String articlePageUrl) {
        return articlePageUrl.split("=")[1];
    }

    @Override
    protected ArticlePage.Visit createVisit(Browser browser) {
        return new Visit(browser);
    }

    public static class Visit {
        private final Browser browser;

        private Visit(Browser browser) {
            this.browser = browser;
        }

        public String title() {
            return browser.getTextContentOfEachElementAt("div.box-container.box-abstract-main h1").get(0);
        }

    }
}
