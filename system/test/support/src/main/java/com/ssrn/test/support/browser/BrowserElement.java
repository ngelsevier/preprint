package com.ssrn.test.support.browser;

public interface BrowserElement {
    String text();

    BrowserElement findChild(String relativeCssSelector);

    BrowserElement[] findChildren(String relativeCssSelector);

    String getAttribute(String attributeName);

    boolean containsCssClassValues(String... classValue);

    boolean isSelected();
}
