package com.ssrn.test.support.browser;

import com.google.common.base.Predicate;
import com.ssrn.test.support.http.HttpUtils;
import com.ssrn.test.support.ssrn.website.pagemodel.Hyperlink;
import com.ssrn.test.support.ssrn.website.pagemodel.WebPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class WebDriverBrowser implements Browser {

    static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowser.class);

    @Override
    public <T> T visit(WebPage<T> webPage) {
        return webPage.visitUsing(this);
    }

    @Override
    public <T> void waitToLoad(WebPage<T> webPage, int timeOutInSeconds) {
        webPage.hasLoadedIn(this, timeOutInSeconds);
    }

    @Override
    public String getLoadedPageSource() {
        return webDriver().getPageSource();
    }

    @Override
    public <T> T click(Hyperlink<T> hyperlink) {
        return hyperlink.clickWith(this);
    }

    @Override
    public void waitUntilCurrentUrlStartsWith(String url, int timeOutInSeconds) {
        waitUntil(driver -> currentUrlStartsWith(url, driver, false), timeOutInSeconds, String.format("waiting for current browser url to start with '%s'", url));
    }

    @Override
    public void waitUntilCurrentUrlHasChangedFrom(String url, int timeOutInSeconds) {
        waitUntil(driver -> !url.equals(driver.getCurrentUrl()), timeOutInSeconds, String.format("waiting for current browser url to change from '%s'", url));
    }

    @Override
    public boolean currentUrlStartsWith(String url, boolean caseInsensitive) {
        return currentUrlStartsWith(url, webDriver(), caseInsensitive);
    }

    @Override
    public String getCurrentLocation() {
        return webDriver().getCurrentUrl();
    }

    @Override
    public void enterTextInField(String cssSelector, String text) {
        WebElement element = webDriver().findElement(By.cssSelector(cssSelector));
        element.clear();
        element.sendKeys(text);
    }

    @Override
    public void clickElement(String cssSelector) {
        clickElement(webDriver().findElement(By.cssSelector(cssSelector)));
    }

    @Override
    public void clickLinkContainingText(String partialLinkText, String cssSelector) {
        clickElement(webDriver().findElements(By.cssSelector(cssSelector))
                .stream()
                .flatMap(webElement -> webElement.findElements(By.partialLinkText(partialLinkText)).stream())
                .findFirst()
                .get()
        );
    }

    @Override
    public void loadUrl(String url) {
        webDriver().get(url);
    }

    @Override
    public String valueOfCurrentLocationQueryParameter(String queryParameterName) {
        return HttpUtils.getQueryParameterValueIn(webDriver().getCurrentUrl(), queryParameterName);
    }

    private static void clickElement(WebElement element) {
        element.click();
    }

    @Override
    public abstract void reset();

    @Override
    public List<BrowserElement> getElementsAt(String cssSelector) {
        return webDriver()
                .findElements(By.cssSelector(cssSelector)).stream()
                .map(WebDriverBrowserElement::new)
                .collect(Collectors.toList());
    }

    @Override
    public void clickBackButton() {
        webDriver().navigate().back();
    }

    @Override
    public boolean isDisplaying(String cssSelector) {
        return displayingElementAt(cssSelector, webDriver());
    }

    @Override
    public String currentUrl() {
        return webDriver().getCurrentUrl();
    }

    @Override
    public List<String> getTextContentOfEachElementAt(String cssSelector) {
        return webDriver()
                .findElements(By.cssSelector(cssSelector)).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    @Override
    public String getTextContentOfElementAt(String cssSelector) {
        return webDriver().findElement(By.cssSelector(cssSelector)).getText();
    }

    @Override
    public void waitUntilNotDisplayingElementAt(String cssSelector, int timeOutInSeconds) {
        waitUntil(driver -> !displayingElementAt(cssSelector, driver), timeOutInSeconds, String.format("waiting for element at '%s' to be hidden", cssSelector));
    }

    @Override
    public void waitUntilDisplayingElementAt(String cssSelector, int timeOutInSeconds) {
        waitUntil(driver -> displayingElementAt(cssSelector, driver), timeOutInSeconds, String.format("waiting for element at '%s' to be displayed", cssSelector));
    }


    @Override
    public abstract void waitUntilIFrameReloadedAfter(Runnable actionThatReloadsIFrame, String iFrameId, Supplier<Boolean> iFrameReloadedCondition, int timeOutInSeconds, String conditionDescription);

    @Override
    public abstract void waitForConditionToBeSatisfiedInPotentiallyReloadingIFrame(Supplier<Boolean> condition, String iFrameId, int timeOutInSeconds, String retryActivityDescription);

    @Override
    public void selectOptionFromDropdown(String cssSelector, String stage){
        Select stageDropdown = new Select(webDriver().findElement(By.cssSelector(cssSelector)));
        stageDropdown.selectByVisibleText(stage);
    }

    protected abstract WebDriver webDriver();

    private boolean currentUrlStartsWith(String url, WebDriver driver, boolean caseInsensitive) {
        String currentUrl = driver.getCurrentUrl();
        return caseInsensitive ? currentUrl.toLowerCase().startsWith(url.toLowerCase()) : currentUrl.startsWith(url);
    }

    protected void waitUntil(Predicate<WebDriver> webDriverPredicate, int timeOutInSeconds, String activityDescription) {
        try {
            new WebDriverWait(webDriver(), timeOutInSeconds)
                    .ignoring(StaleElementReferenceException.class)
                    .until(webDriverPredicate);
        } catch (TimeoutException timeoutException) {
            throw new ContextualizedTimeoutException(timeoutException, activityDescription, timeOutInSeconds);
        }
    }

    @Override
    public boolean displayingElementAt(String cssSelector, WebDriver driver) {
        return driver
                .findElements(By.cssSelector(cssSelector))
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    @Override
    public String getElementAttributeAt(String cssSelector, String attributeName) {
        return webDriver().findElement(By.cssSelector(cssSelector)).getAttribute(attributeName);

    }

    @Override
    public void inIFrame(String iFrameId, Consumer<String> workToDoInIFrame, int secondsToWaitForIFrame) {
        switchFocusToDefault();
        waitUntilDisplayingElementAt(String.format("#%s", iFrameId), secondsToWaitForIFrame);
        switchFocusToIframe(iFrameId);
        workToDoInIFrame.accept(iFrameId);
        switchFocusToDefault();
    }

    @Override
    public void inFrameWithName(String frameName, Consumer<String> workToDoInIFrame, int secondsToWaitForIFrame) {
        switchFocusToDefault();
        waitUntilDisplayingElementAt(String.format("frame[name='%s']", frameName), secondsToWaitForIFrame);
        switchFocusToIframe(frameName);
        workToDoInIFrame.accept(frameName);
        switchFocusToDefault();
    }

    private class ContextualizedTimeoutException extends TimeoutException {
        ContextualizedTimeoutException(TimeoutException timeoutException, String timedOutActivityDescription, int timeoutInSeconds) {
            super(String.format("Timed out after %d seconds %s", timeoutInSeconds, timedOutActivityDescription), timeoutException);
        }
    }
}
