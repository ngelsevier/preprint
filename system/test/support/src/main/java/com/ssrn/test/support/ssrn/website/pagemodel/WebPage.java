package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public interface WebPage<T> {
    void hasLoadedIn(Browser browser, int timeOutInSeconds);

    T visitUsing(Browser browser);

    T loadedIn(Browser browser, boolean caseInsensitive);

    T loadedIn(Browser browser, int timeoutSeconds);
}
