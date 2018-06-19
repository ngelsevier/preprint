package com.ssrn.test.support.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class WebDriverBrowserElement implements BrowserElement {
    private final WebElement webElement;

    public WebDriverBrowserElement(WebElement webElement) {
        this.webElement = webElement;
    }

    @Override
    public String text() {
        return webElement.getText();
    }

    @Override
    public BrowserElement findChild(String relativeCssSelector) {
        return new WebDriverBrowserElement(webElement.findElement(By.cssSelector(relativeCssSelector)));
    }

    @Override
    public BrowserElement[] findChildren(String relativeCssSelector) {
        return webElement.findElements(By.cssSelector(relativeCssSelector)).stream().map(WebDriverBrowserElement::new).collect(Collectors.toList()).toArray(new BrowserElement[]{});
    }

    @Override
    public String getAttribute(String attributeName) {
        return webElement.getAttribute(attributeName);
    }

    @Override
    public boolean containsCssClassValues(String... searchValues) {
        List<String> attributeValues = Arrays.asList(webElement.getAttribute("class").split(" "));
        return Arrays.stream(searchValues).allMatch(value -> attributeValues.contains(value));
    }

    @Override
    public boolean isSelected() {
        return webElement.isSelected();
    }
}
