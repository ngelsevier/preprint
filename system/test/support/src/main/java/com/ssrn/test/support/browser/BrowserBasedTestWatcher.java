package com.ssrn.test.support.browser;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class BrowserBasedTestWatcher extends TestWatcher {

    private static final Logger LOG = Logger.getLogger(BrowserBasedTestWatcher.class.getName());

    private final Browser browser;
    private final String screenshotDirectoryPath;

    public BrowserBasedTestWatcher(Browser browser, String screenshotDirectoryPath) {
        this.browser = browser;
        this.screenshotDirectoryPath = screenshotDirectoryPath;
    }

    @Override
    protected void failed(Throwable e, Description description) {
        String fullyQualifiedTestMethod = String.format("%s.%s", description.getClassName(), description.getMethodName());

        LOG.info((String.format("A browser-based test (%s) failed whilst the browser URL was '%s' and the loaded page source was:\n\n%s",
                fullyQualifiedTestMethod,
                browser.getCurrentLocation(),
                browser.getLoadedPageSource())));

        if (screenshotDirectoryPath != null) {
            browser.saveScreenshotTo(screenshotDirectoryPath, String.format("%s.png", fullyQualifiedTestMethod));
        }
    }
}
