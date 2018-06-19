package com.ssrn.test.support.old_platform_contract_test.configuration;

import com.ssrn.test.support.standalone_test_runner.configuration.ConfigurationBase;
import com.ssrn.test.support.browser.BrowserBasedTestingConfiguration;

import java.util.function.Function;

public class CommandLineBrowserBasedTestingConfiguration extends ConfigurationBase implements BrowserBasedTestingConfiguration {

    private static final String SCREENSHOT_DIRECTORY_PATH = "screenshot-directory-path";
    private static final String VISIBLE = "visible";
    private static final String DRIVER_EXECUTABLE_FILE_PATH = "driver-executable-file-path";
    private static final String DRIVER_LOG_FILE_PATH = "driver-log-file-path";
    private static final String PAGE_LOAD_TIMEOUT_SECONDS = "page-load-timeout-seconds";

    CommandLineBrowserBasedTestingConfiguration(String commandLineArgumentPrefix) {
        super(commandLineArgumentPrefix);
        addParameter(SCREENSHOT_DIRECTORY_PATH, Function.identity(), null, "DIRECTORY_PATH", "when a browser-based test fails, save a screenshot to this directory");
        addParameter(VISIBLE, "false", "display visible browser window");
        addParameter(DRIVER_EXECUTABLE_FILE_PATH, Function.identity(), null, "EXECUTABLE_FILE_PATH", "if specified, overrides default browser driver executable file path");
        addParameter(DRIVER_LOG_FILE_PATH, Function.identity(), null, "LOG_FILE_PATH", "if specified, browser driver will log to this absolute path");
        addParameter(PAGE_LOAD_TIMEOUT_SECONDS, Integer::parseInt, "10", "SECONDS", "time to wait for next page to load in browser");
    }

    @Override
    public String screenshotDirectoryPath() {
        return getValueOf(SCREENSHOT_DIRECTORY_PATH);
    }

    @Override
    public boolean visible() {
        return getValueOf(VISIBLE);
    }

    @Override
    public String driverExecutableFilePath() {
        return getValueOf(DRIVER_EXECUTABLE_FILE_PATH);
    }

    @Override
    public String driverLogFilePath() {
        return getValueOf(DRIVER_LOG_FILE_PATH);
    }

    @Override
    public int pageLoadTimeoutSeconds() {
        return getValueOf(PAGE_LOAD_TIMEOUT_SECONDS);
    }
}
