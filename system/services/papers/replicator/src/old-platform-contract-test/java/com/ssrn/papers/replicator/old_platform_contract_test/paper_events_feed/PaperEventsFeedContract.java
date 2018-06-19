package com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed;

import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformSequentialContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import com.ssrn.test.support.utils.ThreadingUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher.EventMatcher.anEvent;
import static com.ssrn.test.support.http.HttpClient.headers;
import static com.ssrn.test.support.http.HttpClient.queryParameter;
import static com.ssrn.test.support.http.HttpClient.queryParameters;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.lang.Integer.parseInt;
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
public class PaperEventsFeedContract extends SsrnOldPlatformSequentialContractTest {

    private static Event paperDraftedEvent;
    private static Event initialPaperTitleChangedEvent;
    private static Event revisedPaperTitleChangedEvent;
    private static Event paperMadePrivateEvent;
    private static Event paperMadePublicEvent;
    private static Event paperKeywordsChangedEvent;
    private static String abstractId;
    private static Event finalAuthorChangedEvent;
    private static Event paperConsideredIrrelevantEvent;
    private static Event paperSubmissionStageChangedEvent;
    private static Event paperConsideredRelevantEvent;
    private static Event paperWithSubmissionStageChangedToDeletedEvent;
    private static Event paperWithSubmissionStageChangedToRejectedEvent;
    private static Event paperWithRestrictedEvent;
    private static Event paperWithUnrestrictedEvent;
    private static Event deactivatedPaperWithRejectedEvent;
    private static Event paperWithSubmissionStageChangedToApprovedEvent;

    @Test
    public void _001_expectPaperDraftedEventToBeEmittedWhenPaperSubmissionIsStarted() {
        // Given
        String newestEventIdBeforePaperDrafted = ssrnApi().paperEventsStream().getNewestEvent().getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        // When
        abstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .getAbstractId();

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(newestEventIdBeforePaperDrafted, abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterPaperDrafted = ssrnApi().paperEventsStream().getNewEventsAfter(newestEventIdBeforePaperDrafted, abstractId);

        paperDraftedEvent = newEventsAfterPaperDrafted.get(0);
        assertThat(paperDraftedEvent, is(anEvent("DRAFTED", 1)));
        assertThat(paperDraftedEvent.getDataJson(), is(notNullValue()));
        assertThat(paperDraftedEvent.getDataJson(), hasJsonPath("$.isPrivate", is(equalTo(false))));
        assertThat(paperDraftedEvent.getDataJson(), hasJsonPath("$.isConsideredIrrelevant", is(equalTo(false))));
        assertThat(paperDraftedEvent.getDataJson(), hasJsonPath("$.isRestricted", is(equalTo(false))));
        assertThat(paperDraftedEvent.getDataJson(), hasJsonPath("$.isTolerated", is(equalTo(false))));
        assertThat(paperDraftedEvent.getDataJson(), hasJsonPath("$.title", is(equalTo(new Integer(abstractId)))));
        assertThat(paperDraftedEvent.getDataJson(), hasJsonPath("$.authorIds", containsInRelativeOrder(Integer.parseInt(ssrnWebsite().accountId()))));
    }

    @Test
    public void _002_expectPaperTitleChangedEventToBeEmittedWhenPaperTitleIsChangedDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .acceptTermsAndConditions()
                .changeTitleTo("Initial Paper Title");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperDraftedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterDraftedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(paperDraftedEvent.getId(), abstractId);

        initialPaperTitleChangedEvent = newEventsAfterDraftedEvent.get(1);
        assertThat(initialPaperTitleChangedEvent, is(anEvent("TITLE CHANGED", 3)));
        assertThat(initialPaperTitleChangedEvent.getDataJson(), is(notNullValue()));
        assertThat(initialPaperTitleChangedEvent.getDataJson(), hasJsonPath("$.title", is(equalTo("Initial Paper Title"))));
    }

    @Test
    public void _003_expectPaperMadePrivateEventToBeEmittedWhenPaperAvailibilityIsSetToPrivate() {
        //When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .makePaperPrivate();

        //Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(initialPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsTitleChangedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(initialPaperTitleChangedEvent.getId(), abstractId);

        paperMadePrivateEvent = newEventsTitleChangedEvent.get(0);
        assertThat(paperMadePrivateEvent, is(anEvent("MADE PRIVATE", 4)));
        assertThat(paperMadePrivateEvent.getDataJson(), is(nullValue()));

    }

    @Test
    public void _004_expectPaperTitleChangedEventToBeEmittedWhenPaperTitleIsRevisedDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .changeTitleTo("Revised Paper Title");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperMadePrivateEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterInitialTitleChangeEvent = ssrnApi().paperEventsStream().getNewEventsAfter(paperMadePrivateEvent.getId(), abstractId);

        revisedPaperTitleChangedEvent = newEventsAfterInitialTitleChangeEvent.get(0);
        assertThat(revisedPaperTitleChangedEvent, is(anEvent("TITLE CHANGED", 5)));
        assertThat(revisedPaperTitleChangedEvent.getDataJson(), is(notNullValue()));
        assertThat(revisedPaperTitleChangedEvent.getDataJson(), hasJsonPath("$.title", is(equalTo("Revised Paper Title"))));
    }

    @Test
    public void _005_expectPaperTitleChangedEventToBeEmittedWhenPaperTitleIsRevisedViaEditPaperLink() {
        // When
        MyPapersPage.Visit myPapersPageVisit = ssrnWebsite().userHomePage().visitUsing(browser())
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        PaperSubmissionPage.Visit paperSubmissionPageVisit = myPapersPageVisit.editButtonForAbstract(abstractId).clickWith(browser());

        paperSubmissionPageVisit
                .acceptTermsAndConditions()
                .changeTitleTo("Edited Paper Title");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterRevisedTitleChangedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId);
        revisedPaperTitleChangedEvent = newEventsAfterRevisedTitleChangedEvent.get(0);

        Event editedPaperTitleChangedEvent = newEventsAfterRevisedTitleChangedEvent.get(0);
        assertThat(editedPaperTitleChangedEvent, is(anEvent("TITLE CHANGED", 6)));
        assertThat(editedPaperTitleChangedEvent.getDataJson(), is(notNullValue()));
        assertThat(editedPaperTitleChangedEvent.getDataJson(), hasJsonPath("$.title", is(equalTo("Edited Paper Title"))));
    }

    @Test
    public void _006_expectEventFeedToContainAnAdditionalAuthorWhenAuthorIsAddedToPaperDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId());

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterAuthorChangedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId);
        revisedPaperTitleChangedEvent = newEventsAfterAuthorChangedEvent.get(0);

        Event newEventAfterAuthorChangedEvent = newEventsAfterAuthorChangedEvent.get(0);
        assertThat(newEventAfterAuthorChangedEvent, is(anEvent("AUTHOR CHANGED", 7)));
        assertThat(newEventAfterAuthorChangedEvent.getDataJson(), hasJsonPath("$.authorIds", containsInRelativeOrder(Integer.parseInt(ssrnWebsite().accountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));

    }

    @Test
    public void _007_expectEventFeedToContainAnUpdatedAuthorListAfterReorderingAuthorsDuringPaperSubmission() {
        // Given
        MyPapersPage.Visit myPapersPageVisit = ssrnWebsite().userHomePage().visitUsing(browser())
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        PaperSubmissionPage.Visit paperSubmissionPageVisit = myPapersPageVisit.editButtonForAbstract(abstractId).clickWith(browser());

        paperSubmissionPageVisit
                .acceptTermsAndConditions()
                .addAuthorToPaper(ssrnWebsite().secondAdditionalAuthorEmail(), ssrnWebsite().secondAdditionalAuthorAccountId());

        // When
        paperSubmissionPageVisit
                .moveAuthorOrderForAuthor("down", ssrnWebsite().firstAdditionalAuthorAccountId());
        ThreadingUtils.sleepFor(1, TimeUnit.SECONDS);

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(1000, MILLISECONDS)));

        // When
        List<Event> newEventsAfterAuthorReorderedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId);
        revisedPaperTitleChangedEvent = newEventsAfterAuthorReorderedEvent.get(1);
        Event newEventAfterAuthorReorderedEvent = newEventsAfterAuthorReorderedEvent.get(1);

        // Then
        assertThat(newEventAfterAuthorReorderedEvent, is(anEvent("AUTHOR CHANGED", 9)));
        Assert.assertThat(newEventAfterAuthorReorderedEvent.getDataJson(), hasJsonPath("$.authorIds", containsInRelativeOrder(Integer.parseInt(ssrnWebsite().accountId()), Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));

        // Given, When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .moveAuthorOrderForAuthor("up", ssrnWebsite().secondAdditionalAuthorAccountId());
        ThreadingUtils.sleepFor(5, TimeUnit.SECONDS);

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(1000, MILLISECONDS)));

        // When
        newEventsAfterAuthorReorderedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId);
        revisedPaperTitleChangedEvent = newEventsAfterAuthorReorderedEvent.get(0);
        newEventAfterAuthorReorderedEvent = newEventsAfterAuthorReorderedEvent.get(0);


        // Then
        assertThat(newEventAfterAuthorReorderedEvent, is(anEvent("AUTHOR CHANGED", 10)));
        assertThat(newEventAfterAuthorReorderedEvent.getDataJson(), hasJsonPath("$.authorIds", containsInRelativeOrder(Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId()), Integer.parseInt(ssrnWebsite().accountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));
    }

    @Test
    public void _008_expectEventFeedNotToContainTheIdOfAnAuthorRemovedDuringPaperSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .removeAuthorFromPaper(ssrnWebsite().firstAdditionalAuthorAccountId(), ssrnWebsite().accountId());

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        List<Event> newEventsAfterAuthorChangedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId);

        Event newEventAfterAuthorChangedEvent = newEventsAfterAuthorChangedEvent.get(0);
        revisedPaperTitleChangedEvent = newEventsAfterAuthorChangedEvent.get(0);
        assertThat(newEventAfterAuthorChangedEvent, is(anEvent("AUTHOR CHANGED", 11)));
        assertThat(newEventAfterAuthorChangedEvent.getDataJson(), hasJsonPath("$.authorIds", containsInRelativeOrder(Integer.parseInt(ssrnWebsite().accountId()))));
    }

    @Test
    public void _009_expectEventFeedNotToContainTheIdOfTheSubmitterRemovedDuringPaperSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId())
                .makeAuthorPrimary(ssrnWebsite().firstAdditionalAuthorAccountId())
                .removeAuthorFromPaper(ssrnWebsite().accountId(), ssrnWebsite().accountId());

        // Then
        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> events = ssrnApi().paperEventsStream().getNewEventsAfter(revisedPaperTitleChangedEvent.getId(), abstractId);

        finalAuthorChangedEvent = events.get(1);
        assertThat(finalAuthorChangedEvent, is(anEvent("AUTHOR CHANGED", 13)));
        assertThat(finalAuthorChangedEvent.getDataJson(), hasJsonPath("$.authorIds[0]", is(equalTo(Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId())))));
        assertThat(finalAuthorChangedEvent.getDataJson(), hasJsonPath("$.authorIds[1]", is(equalTo(Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId())))));
    }

    @Test
    public void _010_expectPaperMadePublicEventToBeEmittedWhenPaperAvailibilityIsSetToPublic() {
        //When
        Event lastEventBeforeMakingPaperPublic = finalAuthorChangedEvent;

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .makePaperPublic();

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(lastEventBeforeMakingPaperPublic.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsTitleChangedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(lastEventBeforeMakingPaperPublic.getId(), abstractId);

        paperMadePublicEvent = newEventsTitleChangedEvent.get(0);
        assertThat(paperMadePublicEvent, is(anEvent("MADE PUBLIC", 14)));
        assertThat(paperMadePublicEvent.getDataJson(), is(nullValue()));
    }

    @Test
    public void _011_expectKeywordsChangedEventToBeEmittedWhenKeywordsAreChangedDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .setKeywordsTo("Initial, Paper, Keywords");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperMadePublicEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterPaperMadePublicEvent = ssrnApi().paperEventsStream().getNewEventsAfter(paperMadePublicEvent.getId(), abstractId);

        paperKeywordsChangedEvent = newEventsAfterPaperMadePublicEvent.get(0);
        assertThat(paperKeywordsChangedEvent, is(anEvent("KEYWORDS CHANGED", 15)));
        assertThat(paperKeywordsChangedEvent.getDataJson(), is(notNullValue()));
        assertThat(paperKeywordsChangedEvent.getDataJson(), hasJsonPath("$.keywords", is(equalTo("Initial, Paper, Keywords"))));
    }

    @Test
    public void _012_expectSubmissionStageChangedEventToBeEmittedWhenPaperIsSubmitted() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .setAbstractTo(String.format("Some abstract content %s", UUID.randomUUID().toString()));

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .setClassificationToClassifyBySsrn();

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .submitPaper();

        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperKeywordsChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperKeywordsChangedEvent.getId(), abstractId);

        paperSubmissionStageChangedEvent = newEvents.get(0);
        assertThat(paperSubmissionStageChangedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 16)));
        assertThat(paperSubmissionStageChangedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("SUBMITTED"))));
    }

    @Test
    public void _013_expectPaperConsideredIrrelevantEventToBeEmittedWhenPaperMadeIrrelevant() {

        // When
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().adminAccountUsername(), ssrnWebsite().adminAccountPassword());

        ssrnWebsite()
                .userHomePage()
                .visitUsing(browser())
                .sideBar()
                .jensenStageOneQueueLink()
                .clickWith(browser());

        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), false)
                .togglePaperRelevance(abstractId, true);

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperSubmissionStageChangedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperSubmissionStageChangedEvent.getId(), abstractId);

        paperConsideredIrrelevantEvent = newEvents.get(1);
        assertThat(paperConsideredIrrelevantEvent, is(anEvent("CONSIDERED IRRELEVANT", 18)));
        assertThat(paperConsideredIrrelevantEvent.getDataJson(), is(nullValue()));
    }

    @Test
    public void _014_expectPaperConsideredRelevantEventToBeEmittedWhenPaperMadeRelevant() {
        // Given, When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .togglePaperRelevance(abstractId, false);

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperConsideredIrrelevantEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperConsideredIrrelevantEvent.getId(), abstractId);

        paperConsideredRelevantEvent = newEvents.get(0);
        assertThat(paperConsideredRelevantEvent, is(anEvent("CONSIDERED RELEVANT", 19)));
        assertThat(paperConsideredRelevantEvent.getDataJson(), is(nullValue()));
    }

    @Test
    public void _015_expectSubmissionStageChangedWithValueOfDeletedEventToBeEmittedWhenPaperIsDeleted() {
        // Given, When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "DELETED");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperConsideredRelevantEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperConsideredRelevantEvent.getId(), abstractId);

        paperWithSubmissionStageChangedToDeletedEvent = newEvents.get(0);
        assertThat(paperWithSubmissionStageChangedToDeletedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 20)));
        assertThat(paperWithSubmissionStageChangedToDeletedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("DELETED"))));
    }

    @Test
    public void _016_expectSubmissionStageChangedEventWithValueOfRejectedToBeEmittedWhenPaperIsRejected() {
        // Given, When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "REMOVED");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToDeletedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToDeletedEvent.getId(), abstractId);

        paperWithSubmissionStageChangedToRejectedEvent = newEvents.get(0);
        assertThat(paperWithSubmissionStageChangedToRejectedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 21)));
        assertThat(paperWithSubmissionStageChangedToRejectedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("REJECTED"))));
    }

    @Test
    public void _017_expectSubmissionStageChangedEventWithValueOfApprovedToBeEmittedWhenPaperIsApproved() {
        // Given, When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToRejectedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToRejectedEvent.getId(), abstractId);

        paperWithSubmissionStageChangedToApprovedEvent = newEvents.get(0);
        assertThat(paperWithSubmissionStageChangedToApprovedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 22)));
        assertThat(paperWithSubmissionStageChangedToApprovedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("APPROVED"))));
    }

    @Test
    public void _018_expectSubmissionStageChangedWithValueOfRejectedEventToBeEmittedWhenAuthorDeactivatesPaper() {
        // When
         browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAuthorAccountPassword());

        MyPapersPage.Visit myPapersPageVisit = ssrnWebsite().userHomePage().visitUsing(browser())
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        myPapersPageVisit.modify(abstractId).clickWith(browser())
                .deactivatePaper();

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToApprovedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperWithSubmissionStageChangedToApprovedEvent.getId(), abstractId);

        deactivatedPaperWithRejectedEvent = newEvents.get(0);
        assertThat(deactivatedPaperWithRejectedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 23)));
        assertThat(deactivatedPaperWithRejectedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("REJECTED"))));
    }

    @Test
    public void _019_expectSubmissionStageChangedEventWithValueOfRejectedAndRestrictedEventToBeEmittedWhenPaperIsMarkedApprovedRestricted() {
        // Given, When
        browser().switchFocusToMainWindow();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().adminAccountUsername(), ssrnWebsite().adminAccountPassword());

        ssrnWebsite()
                .userHomePage()
                .visitUsing(browser())
                .sideBar()
                .jensenStageOneQueueLink()
                .clickWith(browser());

        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED-RESTRICTED");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(deactivatedPaperWithRejectedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(2)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(deactivatedPaperWithRejectedEvent.getId(), abstractId);

        Event paperWithSubmissionStageChangedToApprovedEvent = newEvents.get(0);
        assertThat(paperWithSubmissionStageChangedToApprovedEvent, is(anEvent("SUBMISSION STAGE CHANGED", 24)));
        assertThat(paperWithSubmissionStageChangedToApprovedEvent.getDataJson(), hasJsonPath("$.submissionStage", is(equalTo("APPROVED"))));

        paperWithRestrictedEvent = newEvents.get(1);
        assertThat(paperWithRestrictedEvent, is(anEvent("RESTRICTED", 25)));
        assertThat(paperWithRestrictedEvent.getDataJson(), is(nullValue()));
    }

    @Test
    public void _020_expectUnrestrictedEventToBeEmittedWhenPaperIsMarkedApproved() {
        // Given, When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // Then
        assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(paperWithRestrictedEvent.getId(), abstractId),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEvents =
                ssrnApi().paperEventsStream().getNewEventsAfter(paperWithRestrictedEvent.getId(), abstractId);

        paperWithUnrestrictedEvent = newEvents.get(0);
        assertThat(paperWithUnrestrictedEvent, is(anEvent("UNRESTRICTED", 26)));
        assertThat(paperWithUnrestrictedEvent.getDataJson(), is(nullValue()));
    }
}
