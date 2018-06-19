package com.ssrn.test.support.browser;

import com.ssrn.test.support.ssrn.website.pagemodel.Hyperlink;
import com.ssrn.test.support.ssrn.website.pagemodel.WebPage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Browser {

    void inIFrame(String iFrameId, Consumer<String> workToDoInIFrame, int secondsToWaitForIFrame);

    void inFrameWithName(String frameName, Consumer<String> workToDoInIFrame, int secondsToWaitForIFrame);

    String valueOfCurrentLocationQueryParameter(String queryParameterName);

    void clickElement(String cssSelector);

    void waitUntilCurrentUrlStartsWith(String url, int timeOutInSeconds);

    boolean currentUrlStartsWith(String url, boolean caseInsensitive);

    String getCurrentLocation();

    void enterTextInField(String cssSelector, String text);

    void loadUrl(String url);

    void clickLinkContainingText(String partialMatchingText, String cssSelector);

    <T> T visit(WebPage<T> webPage);

    <T> void waitToLoad(WebPage<T> webPage, int timeoutSeconds);

    String getLoadedPageSource();

    <TVisit> TVisit click(Hyperlink<TVisit> hyperlink);

    void reset();

    List<String> getTextContentOfEachElementAt(String cssSelector);

    String getTextContentOfElementAt(String cssSelector);

    void waitUntilNotDisplayingElementAt(String cssSelector, int timeOutInSeconds);

    void waitUntilDisplayingElementAt(String cssSelector, int timeOutInSeconds);

    void waitUntilIFrameReloadedAfter(Runnable actionThatReloadsIFrame, String frameId, Supplier<Boolean> iFrameReloadedCondition, int timeOutInSeconds, String timeoutErrorMessage);

    boolean displayingElementAt(String cssSelector, WebDriver driver);

    String getElementAttributeAt(String cssSelector, String attributeName);

    List<BrowserElement> getElementsAt(String cssSelector);

    void clickBackButton();

    void switchFocusToIframe(String frameId);

    void switchFocusToDefault();

    boolean isDisplaying(String cssSelector);

    void close();

    Alert getModalPopup();

    void saveScreenshotTo(String filePath, String file);

    String currentUrl();

    void waitUntilCurrentUrlHasChangedFrom(String url, int timeOutInSeconds);

    void waitForConditionToBeSatisfiedInPotentiallyReloadingIFrame(Supplier<Boolean> condition, String iFrameId, int timeOutInSeconds, String retryActivityDescription);

    void selectOptionFromDropdown(String cssSelector, String stage);

    void switchFocusToPopup();

    void switchFocusToMainWindow();
}
