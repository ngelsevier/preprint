package com.ssrn.test.support.browser;

public interface BrowserBasedTestingConfiguration {

    int pageLoadTimeoutSeconds();

    String screenshotDirectoryPath();

    boolean visible();

    String driverExecutableFilePath();

    String driverLogFilePath();
}
