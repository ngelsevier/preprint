package com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed;

import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformSequentialContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher.EventMatcher.anEvent;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaperRevisionEventsFeedContract extends SsrnOldPlatformSequentialContractTest {

    private static String abstractId;
    private static Event paperSubmissionStageChangedEvent;
    private static Event paperWithSubmissionStageChangedToApprovedEvent;
    private static String revisionAbstractId;
    private static Event revisionSubmittedEvent;

    @Test
    public void _001_userSubmitsANewPaper() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        abstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .getAbstractId();

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .acceptTermsAndConditions()
                .changeTitleTo("Initial Paper Title")
                .setAbstractTo(String.format("Some abstract content %s", UUID.randomUUID().toString()))
                .setClassificationToClassifyBySsrn();

        String newestEventIdBeforePaperIsSubmitted = ssrnApi().paperEventsStream().getNewestEvent().getId();

        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .submitPaper();

        // Then
        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(newestEventIdBeforePaperIsSubmitted, abstractId);

        paperSubmissionStageChangedEvent = newEvents.get(0);
        assertThat(paperSubmissionStageChangedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 4)));
        assertThat(paperSubmissionStageChangedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("SUBMITTED"))));
    }

    @Test
    public void _002_administratorApprovesNewlySubmittedPaper() {

        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().adminAccountUsername(), ssrnWebsite().adminAccountPassword());

        ssrnWebsite()
                .userHomePage()
                .visitUsing(browser())
                .sideBar()
                .jensenStageOneQueueLink()
                .clickWith(browser());

        // When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperSubmissionStageChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperSubmissionStageChangedEvent.getId(), abstractId);

        paperWithSubmissionStageChangedToApprovedEvent = newEvents.get(1);
        assertThat(paperWithSubmissionStageChangedToApprovedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 6)));
        assertThat(paperWithSubmissionStageChangedToApprovedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("APPROVED"))));
    }

    @Test
    public void _003_userCreatesARevisionOfPaper() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        ssrnWebsite()
                .userHomePage()
                .visitUsing(browser())
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        // When
        ssrnWebsite().myPapersPage().loadedIn(browser(), true).createRevision(abstractId);

        PaperSubmissionPage.Visit paperSubmissionPageVisit = ssrnWebsite().paperSubmissionPage().loadedIn(browser(), true);
        revisionAbstractId = paperSubmissionPageVisit.acceptTermsAndConditions().getRevisionAbstractId();

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToApprovedEvent.getId(), revisionAbstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToApprovedEvent.getId(), revisionAbstractId);

        Event revisionDraftedEvent = newEvents.get(0);
        assertThat(revisionDraftedEvent, is(anEvent("DRAFTED", 1)));

        // When
        paperSubmissionPageVisit
                .changeTitleTo("Revision Paper Title")
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId())
                .submitRevision();

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisionDraftedEvent.getId(), revisionAbstractId),
                eventuallySatisfies(hasSize(4)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterRevisionDrafted =
                ssrnApi().paperEventsStream().getNewEventsAfter(revisionDraftedEvent.getId(), revisionAbstractId);

        assertThat(newEventsAfterRevisionDrafted.get(0), is(anEvent("AUTHOR CHANGED", 2)));
        assertThat(newEventsAfterRevisionDrafted.get(1), is(anEvent("TITLE CHANGED", 3)));
        assertThat(newEventsAfterRevisionDrafted.get(2), is(anEvent("AUTHOR CHANGED", 4)));

        revisionSubmittedEvent = newEventsAfterRevisionDrafted.get(3);
        assertThat(revisionSubmittedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 5)));
        assertThat(revisionSubmittedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("SUBMITTED"))));
    }

    @Test
    public void _004_administratorApprovesRevision() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().adminAccountUsername(), ssrnWebsite().adminAccountPassword());

        ssrnWebsite()
                .userHomePage()
                .visitUsing(browser())
                .sideBar()
                .jensenRevisionQueueLink()
                .clickWith(browser());

        // When
        ssrnWebsite().revisionQueuePage().loadedIn(browser(), true)
                .startRevisionReview(revisionAbstractId);

        // Then
        List<Event> newEventsAfterRevisionDrafted =
                ssrnApi().paperEventsStream().getNewEventsAfter(revisionSubmittedEvent.getId(), revisionAbstractId);

        Event revisionUnderReviewEvent = newEventsAfterRevisionDrafted.get(0);
        assertThat(revisionUnderReviewEvent, is(anEvent("SUBMISSION STAGE CHANGED", 6)));
        assertThat(revisionUnderReviewEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("UNDER REVIEW"))));

        // When
        ssrnWebsite().revisionDetailPage().loadedIn(browser(), true)
                .approveMinorRevision(revisionAbstractId);

        ssrnWebsite().paperRevisionAcceptedPage().loadedIn(browser(), true)
                .approve(revisionAbstractId);

        // Then
        List<Event> newEventsAfterRevisionApproved =
                ssrnApi().paperEventsStream().getNewEventsAfter(revisionUnderReviewEvent.getId(), revisionAbstractId);

        Event revisionApprovedEvent = newEventsAfterRevisionApproved.get(0);
        assertThat(revisionApprovedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 7)));
        assertThat(revisionApprovedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("APPROVED"))));

        List<Event> newEventsForOriginalPaperAfterRevisionApproved =
                ssrnApi().paperEventsStream().getNewEventsAfter(revisionApprovedEvent.getId(), abstractId);

        Event originalPaperTitleChangedEvent = newEventsForOriginalPaperAfterRevisionApproved.get(0);
        assertThat(originalPaperTitleChangedEvent, is(anEvent("TITLE CHANGED", 7)));
        assertThat(originalPaperTitleChangedEvent.getDataJson(), hasJsonPath("$.title", is(equalTo("Revision Paper Title"))));

        Event originalPaperAuthorChangedEvent = newEventsForOriginalPaperAfterRevisionApproved.get(1);
        assertThat(originalPaperAuthorChangedEvent.getDataJson(), hasJsonPath("$.authorIds[0]", is(equalTo(Integer.parseInt(ssrnWebsite().accountId())))));
        assertThat(originalPaperAuthorChangedEvent.getDataJson(), hasJsonPath("$.authorIds[1]", is(equalTo(Integer.parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId())))));

        // Then
        List<Event> newEventsAfterOriginalPaperUpdated =
                ssrnApi().paperEventsStream().getNewEventsAfter(originalPaperTitleChangedEvent.getId(), revisionAbstractId);

        Event revisionDeletedEvent = newEventsAfterOriginalPaperUpdated.get(0);
        assertThat(revisionDeletedEvent, is(anEvent("DELETED", 8)));
    }
}
