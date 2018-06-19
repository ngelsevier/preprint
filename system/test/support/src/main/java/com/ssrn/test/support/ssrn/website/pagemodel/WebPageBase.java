package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;
import com.ssrn.test.support.http.WebResource;

import java.util.AbstractMap;
import java.util.List;

public abstract class WebPageBase<T> extends WebResource implements WebPage<T> {
    private final String url;

    @SafeVarargs
    public WebPageBase(String baseUrl, String relativePath, AbstractMap.SimpleEntry<String, List<Object>>... queryParameters) {
        super(baseUrl);
        url = absoluteUrlWithPath(relativePath, queryParameters);
    }

    @Override
    public void hasLoadedIn(Browser browser, int timeOutInSeconds) {
        browser.waitUntilCurrentUrlStartsWith(url, timeOutInSeconds);
    }

    @Override
    public T visitUsing(Browser browser) {
        browser.loadUrl(url);
        return createVisit(browser);
    }

    @Override
    public T loadedIn(Browser browser, boolean caseInsensitive) {
        if (!browser.currentUrlStartsWith(url, caseInsensitive)) {
            throw new RuntimeException(String.format("Expected browser to have loaded URL starting with '%s' but actual URL was '%s'", url, browser.getCurrentLocation()));
        }

        return createVisit(browser);
    }

    @Override
    public T loadedIn(Browser browser, int timeoutSeconds) {
        browser.waitToLoad(this, timeoutSeconds);
        return loadedIn(browser, false);
    }

    protected abstract T createVisit(Browser browser);
}
