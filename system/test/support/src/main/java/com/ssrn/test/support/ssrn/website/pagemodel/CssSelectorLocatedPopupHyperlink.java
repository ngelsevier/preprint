package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class CssSelectorLocatedPopupHyperlink<TVisit> extends UrlChangingHyperlink<TVisit>{
    private final String cssSelector;
    private final WebPageBase<TVisit> webPage;
    private int visitTimeoutSeconds;

    CssSelectorLocatedPopupHyperlink(String cssSelector, WebPageBase<TVisit> webPage, int visitTimeoutSeconds) {
        this.cssSelector = cssSelector;
        this.webPage = webPage;
        this.visitTimeoutSeconds = visitTimeoutSeconds;
    }

    @Override
    public TVisit clickWith(Browser browser) {
        browser.clickElement(cssSelector);
        browser.switchFocusToPopup();
        return webPage.loadedIn(browser, visitTimeoutSeconds);
    }
}
