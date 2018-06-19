package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class CssSelectorLocatedHyperlink<TVisit> extends UrlChangingHyperlink<TVisit> {
    private final String cssSelector;
    private final WebPageBase<TVisit> webPage;
    private int visitTimeoutSeconds;

    CssSelectorLocatedHyperlink(String cssSelector, WebPageBase<TVisit> webPage, int visitTimeoutSeconds) {
        this.cssSelector = cssSelector;
        this.webPage = webPage;
        this.visitTimeoutSeconds = visitTimeoutSeconds;
    }

    @Override
    public TVisit clickWith(Browser browser) {
        ensureUrlHasChangedAfter(b -> b.clickElement(cssSelector), browser, visitTimeoutSeconds);
        return webPage.loadedIn(browser, visitTimeoutSeconds);
    }
}
