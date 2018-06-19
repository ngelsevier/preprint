package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

import java.util.function.Consumer;

public abstract class UrlChangingHyperlink<TVisit> implements Hyperlink<TVisit> {
    void ensureUrlHasChangedAfter(Consumer<Browser> browserAction, Browser browser, int timeoutSeconds) {
        String initialUrl = browser.currentUrl();
        browserAction.accept(browser);
        browser.waitUntilCurrentUrlHasChangedFrom(initialUrl, timeoutSeconds);
    }

    @Override
    public abstract TVisit clickWith(Browser browser);
}
