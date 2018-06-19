package com.ssrn.test.support.browser;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ChromeDriverBrowser extends WebDriverBrowser {
    private static final String DEFAULT_CHROME_DRIVER_EXECUTABLE_FILE_PATH = "/usr/local/bin/chromedriver";

    private final boolean visible;
    private final String chromeDriverLogFilePath;
    private final String chromeDriverExecutableFilePath;
    private final int pageLoadTimeoutSeconds;
    private ChromeDriver webDriver;
    private ChromeDriverService service;
    private String mainWindow;

    public ChromeDriverBrowser(boolean visible, String chromeDriverLogFilePath, String chromeDriverExecutableFilePath, int pageLoadTimeoutSeconds) {
        super();
        this.visible = visible;
        this.chromeDriverLogFilePath = chromeDriverLogFilePath;
        this.chromeDriverExecutableFilePath = chromeDriverExecutableFilePath;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    public WebDriver webDriver() {
        if (webDriver == null) {

            if (chromeDriverLogFilePath != null) {
                System.setProperty("webdriver.chrome.logfile", chromeDriverLogFilePath);
                System.setProperty("webdriver.chrome.verboseLogging", "true");
            }

            service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(new File(chromeDriverExecutableFilePath == null ? DEFAULT_CHROME_DRIVER_EXECUTABLE_FILE_PATH : chromeDriverExecutableFilePath))
                    .usingAnyFreePort()
                    .build();

            try {
                service.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ChromeOptions options = new ChromeOptions();
            options.addArguments("window-size=1600,900");

            if (!visible) {
                options.addArguments("headless");
                options.addArguments("disable-gpu");
            }

            webDriver = new ChromeDriver(service, options);
            webDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeoutSeconds, SECONDS);
        }

        return webDriver;
    }

    @Override
    public void reset() {
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (NoSuchWindowException ignored) {
            }
            webDriver = null;
        }
    }

    @Override
    public void switchFocusToIframe(String frameId) {
        webDriver.switchTo().frame(frameId);
    }

    @Override
    public void switchFocusToPopup() {
        Set<String> windowHandles = webDriver.getWindowHandles();
        Iterator<String> iterator = windowHandles.iterator();
        mainWindow = iterator.next();
        webDriver.switchTo().window(iterator.next());
    }

    public void switchFocusToDefault() {
        webDriver.switchTo().defaultContent();
    }

    @Override
    public void switchFocusToMainWindow() {
        webDriver.switchTo().window(mainWindow);
    }

    @Override
    public void close() {
        if (service != null) {
            service.stop();
            webDriver = null;
        }
    }

    @Override
    public Alert getModalPopup() {
        return webDriver().switchTo().alert();
    }

    @Override
    public void saveScreenshotTo(String directoryPath, String fileName) {
        File screenshotAs = webDriver.getScreenshotAs(OutputType.FILE);

        try {
            FileUtils.copyFile(screenshotAs, new File(directoryPath, fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitUntilIFrameReloadedAfter(Runnable actionThatReloadsIFrame, String iFrameId, Supplier<Boolean> iFrameReloadedCondition, int timeOutInSeconds, String conditionDescription) {
        actionThatReloadsIFrame.run();

        waitForConditionToBeSatisfiedInPotentiallyReloadingIFrame(
                iFrameReloadedCondition,
                iFrameId,
                timeOutInSeconds,
                String.format("waiting for condition to be satisfied: %s after taking an action that causes the %s IFrame to reload", conditionDescription, iFrameId)
        );
    }

    @Override
    public void waitForConditionToBeSatisfiedInPotentiallyReloadingIFrame(Supplier<Boolean> condition, String iFrameId, int timeOutInSeconds, String retryActivityDescription) {
        waitUntil((WebDriver driver) -> {
            try {
                switchFocusToDefault();
                switchFocusToIframe(iFrameId);
                return condition.get();
            } catch (WebDriverException webDriverException) {
                if (webDriverException.getMessage().contains("-32000")) {
                    LOGGER.warn("Exception thrown but intentionally suppressed due to known bug: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2198", webDriverException);
                    return false;
                }
                throw webDriverException;
            }
        }, timeOutInSeconds, retryActivityDescription);
    }
}
