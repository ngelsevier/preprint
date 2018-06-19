package com.ssrn.test.support.ssrn.website;

import com.ssrn.test.support.browser.Browser;
import com.ssrn.test.support.browser.BrowserBasedTestWatcher;
import com.ssrn.test.support.browser.ChromeDriverBrowser;

public class BrowserBasedTestResources {
    private final String screenshotDirectoryPath;
    private final boolean visibleBrowser;
    private final String driverLogFilePath;
    private final String browserDriverExecutableFilePath;
    private final int pageLoadTimeoutSeconds;
    private Browser browser;

    public BrowserBasedTestResources(boolean visibleBrowser, String driverLogFilePath, String screenshotDirectoryPath, int pageLoadTimeoutSeconds) {
        this(null, visibleBrowser, driverLogFilePath, screenshotDirectoryPath, pageLoadTimeoutSeconds);
    }
    public BrowserBasedTestResources(String browserDriverExecutableFilePath, boolean visibleBrowser, String driverLogFilePath, String screenshotDirectoryPath, int pageLoadTimeoutSeconds) {
        this.screenshotDirectoryPath = screenshotDirectoryPath;
        this.visibleBrowser = visibleBrowser;
        this.driverLogFilePath = driverLogFilePath;
        this.browserDriverExecutableFilePath = browserDriverExecutableFilePath;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    public Browser browser() {
        if (browser == null) {
            browser = new ChromeDriverBrowser(visibleBrowser, driverLogFilePath, browserDriverExecutableFilePath, pageLoadTimeoutSeconds);
        }

        return browser;
    }

    public void tearDown() {
        browser().close();
    }

    public BrowserBasedTestWatcher createBrowserBasedTestWatcher() {
        return new BrowserBasedTestWatcher(browser(), screenshotDirectoryPath);
    }

}
