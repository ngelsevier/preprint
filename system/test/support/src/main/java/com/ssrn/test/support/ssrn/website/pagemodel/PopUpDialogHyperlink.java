package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

class PopUpDialogHyperlink<TVisit> extends UrlChangingHyperlink<TVisit> {

    private final String popUpOpenLinkCssSelector;
    private final String linkCssSelector;
    private final WebPageBase<TVisit> webPage;
    private int visitTimeoutSeconds;

    PopUpDialogHyperlink(String popUpOpenLinkCssSelector, String linkCssSelector, WebPageBase<TVisit> webPage, int visitTimeoutSeconds) {
        this.popUpOpenLinkCssSelector = popUpOpenLinkCssSelector;
        this.linkCssSelector = linkCssSelector;
        this.webPage = webPage;
        this.visitTimeoutSeconds = visitTimeoutSeconds;
    }

    @Override
    public TVisit clickWith(Browser browser) {
        browser.waitUntilDisplayingElementAt(popUpOpenLinkCssSelector, visitTimeoutSeconds);
        browser.clickElement(popUpOpenLinkCssSelector);
        browser.waitUntilDisplayingElementAt(linkCssSelector, visitTimeoutSeconds);
        ensureUrlHasChangedAfter(b -> b.clickElement(linkCssSelector), browser, visitTimeoutSeconds);
        return webPage.loadedIn(browser, visitTimeoutSeconds);
    }
}
