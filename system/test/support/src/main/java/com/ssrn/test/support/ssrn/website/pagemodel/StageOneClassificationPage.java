package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

public class StageOneClassificationPage extends WebPageBase<StageOneClassificationPage.Visit> {

    private final SsrnWebsite ssrnWebsite;

    StageOneClassificationPage(String baseUrl, SsrnWebsite ssrnWebsite) {
        super(baseUrl, "/submissions/Stage1queue.cfm");
        this.ssrnWebsite = ssrnWebsite;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser);
    }


    public class Visit {
        private static final int TIME_OUT_IN_SECONDS = 60;
        private final Browser browser;

        public Visit(Browser browser) {

            this.browser = browser;
        }

        public void togglePaperRelevance(String abstractId, boolean makeIrrelevant) {
            startPaperReview(abstractId);
            browser.inFrameWithName("treeFrame", iframeId -> {
                browser.waitUntilDisplayingElementAt(String.format("form[name='form1'] a[href*='abstract_id'][href*='%s']", abstractId), TIME_OUT_IN_SECONDS);

                String excludeFromSearchCheckboxLink = "form[name='form1'] input[type='checkbox'][name='NO_NET_CLASS']";
                browser.waitUntilDisplayingElementAt(excludeFromSearchCheckboxLink, TIME_OUT_IN_SECONDS);
                boolean currentlyIrrelevant = browser.getElementsAt(excludeFromSearchCheckboxLink).get(0).isSelected();
                if (makeIrrelevant != currentlyIrrelevant) {
                    browser.clickElement(excludeFromSearchCheckboxLink);
                    browser.clickElement("form[name='form1'] p input[type='submit'][name='cmdSave']");
                } else {
                    throw new RuntimeException(String.format("Fail to satisfy the expected paper relevance of %s", makeIrrelevant));
                }
            }, TIME_OUT_IN_SECONDS);
        }

        public void changePaperStageTo(String abstractId, String stage) {
            startPaperReview(abstractId);
            browser.inFrameWithName("treeFrame", iframeId -> {
                browser.waitUntilDisplayingElementAt(String.format("form[name='form1'] a[href*='abstract_id'][href*='%s']", abstractId), TIME_OUT_IN_SECONDS);
                String paperStageDropdown = "#cboStatus";
                browser.waitUntilDisplayingElementAt(paperStageDropdown, TIME_OUT_IN_SECONDS);
                browser.selectOptionFromDropdown(paperStageDropdown, stage);
                browser.clickElement("form[name='form1'] p input[type='submit'][name='cmdSave']");
            }, TIME_OUT_IN_SECONDS);
        }

        private void startPaperReview(String abstractId) {
            String abstractSearchTextboxLink = "input[name='txtAbSearch']";
            browser.waitUntilDisplayingElementAt(abstractSearchTextboxLink, TIME_OUT_IN_SECONDS);
            browser.enterTextInField(abstractSearchTextboxLink, abstractId);
            browser.clickElement(".search-subimit");
            String abstractToModify = String.format("form[name='form1'] input[onclick*='%s']", abstractId);
            browser.waitUntilDisplayingElementAt(abstractToModify, TIME_OUT_IN_SECONDS);
            browser.clickElement(abstractToModify);
        }
    }
}