package com.ssrn.fake_old_platform.functional_tests;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Service;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.AbstractMap;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.fake_old_platform.functional_tests.AnEventMatcher.anEvent;
import static com.ssrn.fake_old_platform.functional_tests.EventForEntityMatcher.forEntity;
import static com.ssrn.fake_old_platform.functional_tests.EventOfTypeMatcher.ofType;
import static com.ssrn.fake_old_platform.functional_tests.EventWithEntityVersionMatcher.withEntityVersion;
import static com.ssrn.fake_old_platform.functional_tests.EventWithJsonDataMatcher.withDataSatisfying;
import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class PaperEventsFeedTest extends SsrnFakeOldPlatformTest {

    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());

    @Test
    public void shouldArchiveWorkingPageWhenItContainsThresholdNumberOfEvents() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String abstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .getAbstractId();

        HalEventsPage workingPageAfterStartingPaperSubmission = retrieveEntrypointEventsFeedPage();
        // Guard assertion
        assertThat(workingPageAfterStartingPaperSubmission.getEvents(), hasItem(anEvent(forEntity(abstractId), not(ofType("TITLE CHANGED")))));

        int numberOfEventsOnNewestPage = workingPageAfterStartingPaperSubmission.getEvents().size();
        int expectedNumberOfTitleChangesBeforeLatestPageArchived = Service.PAPER_EVENTS_FEED_EVENTS_PER_PAGE - numberOfEventsOnNewestPage;
        // Guard assertion
        assertThat(expectedNumberOfTitleChangesBeforeLatestPageArchived, is(greaterThanOrEqualTo(0))); // Guard Assertion
        String previousPageUrl = workingPageAfterStartingPaperSubmission.getLinks().getPreviousArchive().getHref();

        for (int i = 0; i < expectedNumberOfTitleChangesBeforeLatestPageArchived; i++) {
            ssrnWebsite()
                    .paperSubmissionPage().loadedIn(browser(), false)
                    .changeTitleTo(String.format("Foo Title %s", i));

            HalEventsPage workingPageAfterChangingTitleToFewTimesToCrossThreshold = retrieveEntrypointEventsFeedPage();
            // Guard assertion
            assertThat("Did not expect newest page to be archived yet", previousPageUrl, is(equalTo(workingPageAfterChangingTitleToFewTimesToCrossThreshold.getLinks().getPreviousArchive().getHref())));
        }

        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .changeTitleTo("Foo Title final");

        // Then
        HalEventsPage workingPageAfterChangingTitleEnoughTimesToCrossThreshold = retrieveEntrypointEventsFeedPage();
        assertThat("Expected newest page to be archived by now", previousPageUrl,
                is(not(equalTo(workingPageAfterChangingTitleEnoughTimesToCrossThreshold.getLinks().getPreviousArchive().getHref()))));

        HalEventsPage previousPageAfterChangingTitleEnoughTimesToCrossThreshold = httpClient()
                .get(workingPageAfterChangingTitleEnoughTimesToCrossThreshold.getLinks().getPreviousArchive().getHref(), headers(acceptApplicationJsonHeader, authorizationHeader))
                .readEntity(HalEventsPage.class);
        assertThat("Expected drafted event to have been archived", previousPageAfterChangingTitleEnoughTimesToCrossThreshold.getEvents(), hasItem(anEvent(forEntity(abstractId), not(ofType("TITLE CHANGED")))));
    }

    @Test
    public void shouldContainEventsWithConsecutivelyIncreasingVersionNumbersForUpdatedPaperThatWasCreatedBeforeTheEventFeedExisted() {
        // Given
        FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();
        int abstractId = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("A pre-event feed paper %s", UUID.randomUUID()), null, new String[]{"1"}, false, false, false, "SUBMITTED").getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        MyPapersPage.Visit myPapersPageVisit = ssrnWebsite().userHomePage().loadedIn(browser(), false)
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        String paperId = Integer.toString(abstractId);
        PaperSubmissionPage.Visit paperSubmissionPageVisit = myPapersPageVisit
                .editButtonForAbstract(paperId)
                .clickWith(browser());

        String updatedTitle = String.format("Updated paper %s", UUID.randomUUID());
        paperSubmissionPageVisit
                .acceptTermsAndConditions()
                .changeTitleTo(updatedTitle);

        // When
        List<Event> events = retrieveEntrypointEventsFeedPage()
                .getEvents();

        assertThat(events, hasItem(anEvent(
                forEntity(paperId),
                ofType("TITLE CHANGED"),
                withEntityVersion(2),
                withDataSatisfying(hasJsonPath("$.title", is(equalTo(updatedTitle))))
        )));
    }

    private HalEventsPage retrieveEntrypointEventsFeedPage() {
        return httpClient()
                .get(ssrnAbsoluteUrl("/rest/papers/events"), headers(acceptApplicationJsonHeader, authorizationHeader))
                .readEntity(HalEventsPage.class);
    }

}
