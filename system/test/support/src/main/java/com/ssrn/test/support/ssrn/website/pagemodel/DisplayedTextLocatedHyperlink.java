package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class DisplayedTextLocatedHyperlink<TVisit> extends UrlChangingHyperlink<TVisit> {

    private final String cssSelector;
    private final String displayedText;
    private final WebPage<TVisit> webPage;
    private final int visitTimeoutSeconds;

    DisplayedTextLocatedHyperlink(String displayedText, String cssSelector, WebPage<TVisit> webPage, int visitTimeoutSeconds) {
        this.cssSelector = cssSelector;
        this.displayedText = displayedText;
        this.webPage = webPage;
        this.visitTimeoutSeconds = visitTimeoutSeconds;
    }

    @Override
    public TVisit clickWith(Browser browser) {
        ensureUrlHasChangedAfter(b -> b.clickLinkContainingText(displayedText, cssSelector), browser, visitTimeoutSeconds);
        return webPage.loadedIn(browser, visitTimeoutSeconds);
    }
}
