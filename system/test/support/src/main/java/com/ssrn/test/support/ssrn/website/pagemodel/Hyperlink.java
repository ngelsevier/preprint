package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public interface Hyperlink<TVisit> {
    TVisit clickWith(Browser browser);
}
