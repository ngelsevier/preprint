package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PaperSubmissionPage extends WebPageBase<PaperSubmissionPage.Visit> {

    private static final String AUTHORS_IFRAME_ID = "iFrameAuthors";
    private int pageLoadTimeoutSeconds;

    PaperSubmissionPage(String baseUrl, int pageLoadTimeoutSeconds) {
        super(baseUrl, "/submissions/SimpleSubmission.cfm");
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser, pageLoadTimeoutSeconds);
    }

    public static class Visit {
        private final Browser browser;
        private int timeoutInSeconds;
        private Visit(Browser browser, int pageLoadTimeoutSeconds) {
            this.browser = browser;
            timeoutInSeconds = pageLoadTimeoutSeconds;
        }

        public Visit acceptTermsAndConditions() {
            browser.waitUntilDisplayingElementAt(".warning input[value=Continue]", timeoutInSeconds);
            browser.clickElement(".warning input[value=Continue]");
            return this;
        }

        public Visit changeTitleTo(String title) {
            String editPaperLink = "#paperTitle #EditTitleLink";
            browser.waitUntilDisplayingElementAt(editPaperLink, timeoutInSeconds);
            browser.clickElement(editPaperLink);
            browser.waitUntilNotDisplayingElementAt("#paperTitle", timeoutInSeconds);
            browser.enterTextInField("#titleForm #ab_title", title);
            browser.clickLinkContainingText("Save", "#titleForm");
            browser.waitUntilDisplayingElementAt("#paperTitle", timeoutInSeconds);
            return this;
        }

        public String getAbstractId() {
            return browser.valueOfCurrentLocationQueryParameter("AbstractID");
        }

        public Visit addAuthorToPaper(String authorEmail, String authorId) {
            browser.inIFrame("iFrameAuthors", iFrameId -> {
                ensureAuthorsDialogOpen();

                String authorEmailSearchBox = "#authorForm #txtcoAuthorEmail";
                browser.waitUntilDisplayingElementAt(authorEmailSearchBox, timeoutInSeconds);
                browser.enterTextInField(authorEmailSearchBox, authorEmail);
                String authorSearchButton = "#authorForm a.smallBtn";
                browser.waitUntilDisplayingElementAt(authorSearchButton, timeoutInSeconds);
                sleepFor(3, SECONDS);

                String authorFoundLink = String.format("#authorForm #tblResultAuthors a[onclick*='%s']", authorId);

                browser.waitUntilIFrameReloadedAfter(() -> browser.clickElement(authorSearchButton),
                        iFrameId,
                        () -> browser.isDisplaying(authorFoundLink),
                        timeoutInSeconds,
                        String.format("element at '%s' is visible", authorFoundLink)
                );

                String authorAddedElement = String.format("#authorForm #tblAssistants input[name=rdoAuthType_%s]", authorId);

                browser.waitUntilIFrameReloadedAfter(() -> browser.clickElement(authorFoundLink),
                        iFrameId,
                        () -> browser.isDisplaying(authorAddedElement),
                        timeoutInSeconds,
                        String.format("element at '%s' is visible", authorAddedElement)
                );

            }, timeoutInSeconds);

            return this;
        }

        public Visit removeAuthorFromPaper(String authorId, String submitterId) {
            browser.inIFrame("iFrameAuthors", iFrameId -> {

                String removeButton = String.format("#authorForm #tblAssistants a[title*='Remove'][onclick*='%s']", authorId);

                browser.waitUntilIFrameReloadedAfter(this::ensureAuthorsDialogOpen,
                        iFrameId,
                        () -> browser.isDisplaying(removeButton),
                        timeoutInSeconds,
                        String.format("element at '%s' is visible", removeButton)
                );

                String removedAuthorElement = String.format("#authorForm #tblAssistants input[name=rdoAuthType_%s]", authorId);

                browser.waitUntilIFrameReloadedAfter(() -> {
                            browser.clickElement(removeButton);

                            if (authorId.equals(submitterId)) {
                                sleepFor(2, SECONDS);
                                browser.getModalPopup().accept();
                            }
                        },
                        iFrameId,
                        () -> !browser.isDisplaying(removedAuthorElement),
                        timeoutInSeconds,
                        String.format("element at '%s' is not visible", removedAuthorElement)
                );
            }, timeoutInSeconds);

            return this;
        }

        public Visit makeAuthorPrimary(String authorId) {
            browser.inIFrame("iFrameAuthors", iFrameId -> {
                ensureAuthorsDialogOpen();
                String contactAuthorLink = String.format("#authorForm #tblAssistants a[onclick*=\"'contactauthor','%s'\"]", authorId);
                browser.waitUntilDisplayingElementAt(contactAuthorLink, timeoutInSeconds);
                browser.clickElement(contactAuthorLink);
                browser.switchFocusToDefault();
            }, timeoutInSeconds);

            return this;
        }

        public void moveAuthorOrderForAuthor(String direction, String authorId) {
            browser.inIFrame("iFrameAuthors", iFrameId -> {
                ensureAuthorsDialogOpen();
                browser.waitUntilDisplayingElementAt("#authorForm[style*='block']", timeoutInSeconds);
                String authorMovementLink = String.format("#authorForm #tblAssistants a[onclick*=\"'%s','%s'\"]", direction, authorId);
                browser.waitUntilDisplayingElementAt(authorMovementLink, timeoutInSeconds);
                browser.clickElement(authorMovementLink);
            }, timeoutInSeconds);
        }

        private void ensureAuthorsDialogOpen() {
            String paperAuthorLink = ".paperAuthors a:first-of-type";
            String authorsDialogForm = "#authorForm";

            browser.waitForConditionToBeSatisfiedInPotentiallyReloadingIFrame(() -> browser.isDisplaying(paperAuthorLink) || browser.isDisplaying(authorsDialogForm),
                    "iFrameAuthors",
                    timeoutInSeconds,
                    String.format("either element at '%s' is visible ir element at '%s' is visible", paperAuthorLink, authorsDialogForm)
            );

            if (!browser.isDisplaying(authorsDialogForm)) {
                browser.waitUntilIFrameReloadedAfter(() -> browser.clickElement(paperAuthorLink), AUTHORS_IFRAME_ID,
                        () -> browser.isDisplaying(authorsDialogForm),
                        timeoutInSeconds,
                        String.format("element at '%s' is visible", authorsDialogForm)
                );
            }
        }

        public Visit makePaperPrivate() {
            String makePaperPrivateLink = "#areaAvailabilityEdit input[onclick*='setPriAvailable']";
            changePaperAvailabilityUsing(makePaperPrivateLink);
            return this;
        }

        public Visit makePaperPublic() {
            String makePaperPublicLink = "#areaAvailabilityEdit input[onclick*='setPubAvailable']";
            changePaperAvailabilityUsing(makePaperPublicLink);
            return this;
        }

        private void changePaperAvailabilityUsing(String paperAvailabilityLink) {
            if (browser.isDisplaying(paperAvailabilityLink)) {
                browser.clickElement(paperAvailabilityLink);
            } else {
                String changeAvailabilityLink = "#areaAvailability #EditAvailability";
                browser.waitUntilDisplayingElementAt(changeAvailabilityLink, timeoutInSeconds);
                browser.clickElement(changeAvailabilityLink);
                browser.waitUntilDisplayingElementAt(paperAvailabilityLink, timeoutInSeconds);
                browser.clickElement(paperAvailabilityLink);
            }
        }

        public Visit setKeywordsTo(String keywordsContent) {
            String abstractHeaderLink = "#abstractArea #abHeader";
            browser.waitUntilDisplayingElementAt(abstractHeaderLink, timeoutInSeconds);
            browser.clickElement(abstractHeaderLink);
            String keywordsFieldName = "#abstractArea #abstractForm #ab_keywords";
            browser.waitUntilDisplayingElementAt(keywordsFieldName, timeoutInSeconds);
            browser.enterTextInField(keywordsFieldName, keywordsContent);
            browser.clickElement("#abstractArea #abstractForm a[title='Save & Close this section']");

            return this;
        }

        public Visit setAbstractTo(String abstractContent) {
            String abstractHeaderLink = "#abstractArea #abHeader";
            browser.waitUntilDisplayingElementAt(abstractHeaderLink, timeoutInSeconds);
            browser.clickElement(abstractHeaderLink);
            String abstractFormLink = "#abstractArea #abstractForm #ab_content";
            browser.waitUntilDisplayingElementAt(abstractFormLink, timeoutInSeconds);
            browser.enterTextInField(abstractFormLink, abstractContent);
            browser.clickElement("#abstractArea #abstractForm a[title='Save & Close this section']");

            return this;
        }

        public Visit setClassificationToClassifyBySsrn() {
            String classificationHeaderLink = "#paperClass";
            browser.waitUntilDisplayingElementAt(classificationHeaderLink, timeoutInSeconds);
            browser.clickElement(classificationHeaderLink);
            String ssrnClassificationLink = "#classForm input[type='radio'][value='T']";
            browser.waitUntilDisplayingElementAt(ssrnClassificationLink, timeoutInSeconds);
            browser.clickElement(ssrnClassificationLink);
            browser.clickElement("#classForm a[title='Save & Close this section']");

            return this;
        }

        public void submitPaper() {
            String submitPaperLink = "#submitDiv #btnSubmit";
            browser.waitUntilDisplayingElementAt(submitPaperLink, timeoutInSeconds);
            browser.clickElement(submitPaperLink);
            String confirmCheckboxLink = "#explainText #certifyTrue";
            browser.waitUntilDisplayingElementAt(confirmCheckboxLink, timeoutInSeconds);
            browser.clickElement(confirmCheckboxLink);
            String submitToSsrnLink = "#explainText input[value='Submit to SSRN']";
            browser.waitUntilDisplayingElementAt(submitToSsrnLink, timeoutInSeconds);
            browser.clickElement(submitToSsrnLink);
        }

        public void submitRevision() {
            String submitPaperLink = "#submitDiv #btnSubmit";
            browser.waitUntilDisplayingElementAt(submitPaperLink, timeoutInSeconds);
            browser.clickElement(submitPaperLink);

            String confirmCheckboxLink = "#explainText #certifyTrue";
            browser.waitUntilDisplayingElementAt(confirmCheckboxLink, timeoutInSeconds);
            browser.clickElement(confirmCheckboxLink);

            String submitToSsrnLink = "#explainText input[value='Submit Revision']";
            browser.waitUntilDisplayingElementAt(submitToSsrnLink, timeoutInSeconds);
            browser.clickElement(submitToSsrnLink);
        }

        public String getRevisionAbstractId() {
            return browser.getElementsAt("#AbstractID").get(0).getAttribute("value");
        }
    }
}
