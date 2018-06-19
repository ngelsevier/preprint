package com.ssrn.authors.replicator.old_platform_contract_test.author_events_feed;

import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformSequentialContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Random;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.authors.replicator.old_platform_contract_test.author_events_feed.matcher.EventWithTypeAndVersionMatcher.anEventWithTypeAndVersion;
import static com.ssrn.authors.replicator.old_platform_contract_test.author_events_feed.matcher.EventWithTypeMatcher.anEventWithType;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorEventsFeedContract extends SsrnOldPlatformSequentialContractTest {

    private static Event authorNameChangedEvent;
    private static String abstractId;
    private static Event authorUnregisteredEventAfterPaperDeleted;
    private static Event eventAfterPaperApproved;
    private static Event eventAfterPaperDeactivated;
    private static Event eventAfterPaperReapproved;
    @Test
    public void _001_expectThatWeCanChangeParticipantDisplayNameOfAnAuthor() {
        // Given
        int uniqueNumber = new Random().nextInt(9999);
        String firstName = String.format("Test %d", uniqueNumber);
        String lastName = "User 1";
        String fullName = String.format("%s %s", firstName, lastName);

        String newestEventIdBeforeAuthorNameChanged = ssrnApi().authorEventsStream().getNewestEvent().getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions();

        // When
        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo(firstName, lastName)
                .submitUpdates();

        // Then
        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorNameChanged, ssrnWebsite().accountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterNameChanged = ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorNameChanged, ssrnWebsite().accountId());

        authorNameChangedEvent = newEventsAfterNameChanged.get(0);
        assertThat(authorNameChangedEvent, is(anEventWithType("NAME CHANGED")));
        assertThat(authorNameChangedEvent.getDataJson(), is(notNullValue()));
        assertThat(authorNameChangedEvent.getDataJson(), hasJsonPath("$.name", is(equalTo(fullName))));
    }

    @Test
    public void _002_expectThatEntityVersionIsIncreasedAfterChangeOfAuthorName() {
        // Given
        String firstName = String.format("Test %s", randomString());
        String lastName = "User 1";
        String fullName = String.format("%s %s", firstName, lastName);
        int currentEntityVersion = authorNameChangedEvent.getEntityVersion();
        String newestEventIdBeforeAuthorNameChanged = ssrnApi().authorEventsStream().getNewestEvent().getId();

        // When
        ssrnWebsite().personalInformationPage(ssrnWebsite().accountId()).loadedIn(browser(), true).enterPublicDisplayNameTo(firstName, lastName).submitUpdates();

        // Then
        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorNameChanged, ssrnWebsite().accountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterNameChanged = ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorNameChanged, ssrnWebsite().accountId());

        authorNameChangedEvent = newEventsAfterNameChanged.get(0);
        assertThat(authorNameChangedEvent, is(anEventWithTypeAndVersion("NAME CHANGED", currentEntityVersion + 1)));
        assertThat(authorNameChangedEvent.getDataJson(), is(notNullValue()));
        assertThat(authorNameChangedEvent.getDataJson(), hasJsonPath("$.name", is(equalTo(fullName))));
    }

    @Test
    public void _003_expectRegisteredEventToBeEmittedWhenANonExistentAuthorIsAddedToAPaper() {
        // Given
        ssrnTestDataClient().ensureNoPapersAuthoredBy(ssrnWebsite().thirdAdditionalAuthorAccountId());

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());

        String firstName = String.format("Test %s", randomString());
        String lastName = "User 2";

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId()).personalInfoLink())
                .enterPublicDisplayNameTo(firstName, lastName)
                .submitUpdates();

        String newestEventIdBeforeAuthorRegistered = ssrnApi().authorEventsStream().getNewestEvent().getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions();

        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorRegistered, ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterAuthorRegistered = ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorRegistered, ssrnWebsite().thirdAdditionalAuthorAccountId());

        Event authorRegisteredEvent = newEventsAfterAuthorRegistered.get(0);
        assertThat(authorRegisteredEvent, is(anEventWithType("REGISTERED")));
        assertThat(authorRegisteredEvent.getDataJson(), is(notNullValue()));
        assertThat(authorRegisteredEvent.getDataJson(), hasJsonPath("$.name", is(equalTo(String.format("%s %s", firstName, lastName)))));
    }

    @Test
    public void _004_expectUnregisteredEventFromEventFeedAfterBeingRemovedAsAuthorDuringPaperSubmission() {

        // Given
        String newestEventIdBeforeAuthorUnregistered = ssrnApi().authorEventsStream().getNewestEvent().getId();
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .removeAuthorFromPaper(ssrnWebsite().thirdAdditionalAuthorAccountId(), ssrnWebsite().accountId());

        // When
        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorUnregistered, ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterAuthorUnregistered = ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeAuthorUnregistered, ssrnWebsite().thirdAdditionalAuthorAccountId());

        Event authorRegisteredEvent = newEventsAfterAuthorUnregistered.get(0);
        assertThat(authorRegisteredEvent, is(anEventWithType("UNREGISTERED")));
    }

    @Test
    public void _005_expectNoFurtherRegisteredEventsToBeEmittedWhenAnExistingAuthorIsAddedToAPaper() {
        // Given
        String newestEventId = ssrnApi().authorEventsStream().getNewestEvent().getId();

        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        // Then
        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(newestEventId, ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterAuthorRegistered = ssrnApi().authorEventsStream().getNewEventsAfter(newestEventId, ssrnWebsite().thirdAdditionalAuthorAccountId());

        Event theEvent = newEventsAfterAuthorRegistered.get(0);
        assertThat(theEvent, is(anEventWithType("REGISTERED")));

        String newestEventIdAfterAuthorRegistered = ssrnApi().authorEventsStream().getNewestEvent().getId();

        // When
        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions();

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        assertThat(ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdAfterAuthorRegistered, ssrnWebsite().thirdAdditionalAuthorAccountId()), hasSize(0));
    }

    @Test
    public void _006_expectAuthorVersionToRemainUnchangedWhilstParticipantIsNotAnAuthor() {
        // Given
        String newestEventIdBeforeRenamingAuthor = ssrnApi().authorEventsStream().getNewestEvent().getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId()).personalInfoLink())
                .enterPublicDisplayNameTo(String.format("Test %s", randomString()), "User 2")
                .submitUpdates();

        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeRenamingAuthor, ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        Event lastEventBeforeAuthorUnregistered = ssrnApi().authorEventsStream().getNewEventsAfter(newestEventIdBeforeRenamingAuthor, ssrnWebsite().thirdAdditionalAuthorAccountId()).get(0);

        ssrnTestDataClient().ensureNoPapersAuthoredBy(ssrnWebsite().thirdAdditionalAuthorAccountId());

        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(lastEventBeforeAuthorUnregistered.getId(), ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterAuthorUnregistered = ssrnApi().authorEventsStream().getNewEventsAfter(lastEventBeforeAuthorUnregistered.getId(), ssrnWebsite().thirdAdditionalAuthorAccountId());
        Event authorUnregisteredEvent = newEventsAfterAuthorUnregistered.get(0);
        assertThat(authorUnregisteredEvent, is(anEventWithTypeAndVersion("UNREGISTERED", lastEventBeforeAuthorUnregistered.getEntityVersion() + 1)));

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId()).personalInfoLink())
                .enterPublicDisplayNameTo("Test", "User 2 Updated")
                .submitUpdates();

        // When, Then
        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(authorUnregisteredEvent.getId(), ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(0)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        // When
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions();

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        // Then
        assertThat(() -> ssrnApi().authorEventsStream().getNewEventsAfter(authorUnregisteredEvent.getId(), ssrnWebsite().thirdAdditionalAuthorAccountId()),
                eventuallySatisfies(hasSize(1)).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<Event> newEventsAfterAuthorRegistered = ssrnApi().authorEventsStream().getNewEventsAfter(authorUnregisteredEvent.getId(), ssrnWebsite().thirdAdditionalAuthorAccountId());
        Event authorRegisteredEvent = newEventsAfterAuthorRegistered.get(0);
        assertThat(authorRegisteredEvent, is(anEventWithTypeAndVersion("REGISTERED", authorUnregisteredEvent.getEntityVersion() + 1)));
        assertThat(authorRegisteredEvent.getDataJson(), is(notNullValue()));
        assertThat(authorRegisteredEvent.getDataJson(), hasJsonPath("$.name", is(equalTo("Test User 2 Updated"))));
    }

    @Ignore("WIP-316")
    @Test
    public void _007_expectUnregisteredEventFromEventFeedAfterHisOnlyPaperHasBeenDeleted() {
        // Given
        ssrnTestDataClient().ensureNoPapersAuthoredBy(ssrnWebsite().accountId());
        String newestAuthorEventIdBeforePaperSubmission = ssrnApi().authorEventsStream().getNewestEvent().getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        PaperSubmissionPage.Visit paperSubmissionPageVisit = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        abstractId = paperSubmissionPageVisit.getAbstractId();

        // When
        List<Event> eventsAfterPaperSubmission = ssrnApi().authorEventsStream().getNewEventsAfter(newestAuthorEventIdBeforePaperSubmission, ssrnWebsite().accountId());
        Event authorRegisteredEvent = eventsAfterPaperSubmission.get(0);

        // Then
        assertThat(authorRegisteredEvent, is(anEventWithType("REGISTERED")));

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

        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "DELETED");

        // When
        List<Event> newEventsAfterAuthorUnregistered = ssrnApi().authorEventsStream().getNewEventsAfter(authorRegisteredEvent.getId(), ssrnWebsite().accountId());

        // Then
        authorUnregisteredEventAfterPaperDeleted = newEventsAfterAuthorUnregistered.get(0);
        assertThat(authorUnregisteredEventAfterPaperDeleted, is(anEventWithType("UNREGISTERED")));
    }

    @Ignore("WIP-316")
    @Test
    public void _008_expectRegisteredEventFromEventFeedAfterHisOnlyPaperHasBeenApproved() {
        // Given
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // When
        List<Event> newEventsAfterPaperApproved = ssrnApi().authorEventsStream().getNewEventsAfter(authorUnregisteredEventAfterPaperDeleted.getId(), ssrnWebsite().accountId());

        eventAfterPaperApproved = newEventsAfterPaperApproved.get(0);

        // Then
        assertThat(eventAfterPaperApproved, is(anEventWithType("REGISTERED")));
    }

    @Ignore("WIP-316")
    @Test
    public void _009_expectUnregisteredEventFromEventFeedAfterHisOnlyPaperHasBeenDeactivated() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        MyPapersPage.Visit myPapersPageVisit = ssrnWebsite().userHomePage().visitUsing(browser())
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        myPapersPageVisit.modify(abstractId).clickWith(browser())
                .deactivatePaper();

        // When
        List<Event> newEventsAfterPaperDeactivated = ssrnApi().authorEventsStream().getNewEventsAfter(eventAfterPaperApproved.getId(), ssrnWebsite().accountId());

        eventAfterPaperDeactivated = newEventsAfterPaperDeactivated.get(0);

        // Then
        assertThat(eventAfterPaperDeactivated, is(anEventWithType("UNREGISTERED")));
    }

    @Ignore("WIP-316")
    @Test
    public void _010_expectRegisteredEventFromEventFeedAfterHisOnlyPaperHasBeenApproved() {
        // Given
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
                .changePaperStageTo(abstractId, "APPROVED");

        // When
        List<Event> newEventsAfterPaperApprovedRestricted = ssrnApi().authorEventsStream().getNewEventsAfter(eventAfterPaperDeactivated.getId(), ssrnWebsite().accountId());

        eventAfterPaperReapproved = newEventsAfterPaperApprovedRestricted.get(0);

        // Then
        assertThat(eventAfterPaperReapproved, is(anEventWithType("REGISTERED")));
    }

    @Ignore("WIP-316")
    @Test
    public void _011_expectRegisteredEventFromEventFeedAfterHisOnlyPaperHasBeenRejected() {
        // Given
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
                .changePaperStageTo(abstractId, "REMOVED");

        // When
        List<Event> newEventsAfterPaperApprovedRestricted = ssrnApi().authorEventsStream().getNewEventsAfter(eventAfterPaperReapproved.getId(), ssrnWebsite().accountId());
        Event eventAfterPaperApprovedRestricted = newEventsAfterPaperApprovedRestricted.get(0);

        // Then
        assertThat(eventAfterPaperApprovedRestricted, is(anEventWithType("UNREGISTERED")));
    }
    private static String randomString() {
        int uniqueNumber = new Random().nextInt(9999);
        return Integer.toString(uniqueNumber);
    }
}