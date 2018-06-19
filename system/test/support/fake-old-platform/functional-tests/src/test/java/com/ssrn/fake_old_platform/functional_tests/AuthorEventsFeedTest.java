package com.ssrn.fake_old_platform.functional_tests;

import com.ssrn.fake_old_platform.Service;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.ssrn.website.pagemodel.PersonalInformationPage;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.AbstractMap;
import java.util.Random;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.fake_old_platform.functional_tests.AnEventMatcher.anEvent;
import static com.ssrn.fake_old_platform.functional_tests.EventForEntityMatcher.forEntity;
import static com.ssrn.fake_old_platform.functional_tests.EventOfTypeMatcher.ofType;
import static com.ssrn.fake_old_platform.functional_tests.EventWithJsonDataMatcher.withDataSatisfying;
import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class AuthorEventsFeedTest extends SsrnFakeOldPlatformTest {

    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());

    @Test
    public void shouldArchiveWorkingPageWhenItContainsThresholdNumberOfEvents() {
        // Given
        int uniqueNumber = new Random().nextInt(9999);
        String firstName = String.format("Test %d", uniqueNumber);
        String lastName = "User 1";
        String initialFullName = String.format("%s %s", firstName, lastName);

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().authorForNameChangeEmail(), "");

        browser()
                .visit(ssrnWebsite().userHomePage());

        PersonalInformationPage.Visit personalInformationPageVisit = browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().authorForNameChangeAccountId()).personalInfoLink());

        personalInformationPageVisit
                .enterPublicDisplayNameTo(firstName, lastName)
                .submitUpdates();

        HalEventsPage workingPageAfterStartingPaperSubmissionAndChangingNameOnce = retrieveEntrypointEventsFeedPage();

        int numberOfEventsOnNewestPage = workingPageAfterStartingPaperSubmissionAndChangingNameOnce.getEvents().size();
        int expectedNumberOfNameChangesBeforeLatestPageArchived = Service.AUTHOR_EVENTS_FEED_EVENTS_PER_PAGE - numberOfEventsOnNewestPage;

        // Guard assertion
        assertThat(expectedNumberOfNameChangesBeforeLatestPageArchived, is(greaterThanOrEqualTo(0)));
        String previousPageUrl = workingPageAfterStartingPaperSubmissionAndChangingNameOnce.getLinks().getPreviousArchive().getHref();

        for (int i = 0; i < expectedNumberOfNameChangesBeforeLatestPageArchived; i++) {
            personalInformationPageVisit
                    .enterPublicDisplayNameTo(String.format("%s %s", firstName, i), lastName)
                    .submitUpdates();

            HalEventsPage workingPageAfterChangingNameToFewTimesToCrossThreshold = retrieveEntrypointEventsFeedPage();
            // Guard assertion
            assertThat("Did not expect newest page to be archived yet", previousPageUrl, is(equalTo(workingPageAfterChangingNameToFewTimesToCrossThreshold.getLinks().getPreviousArchive().getHref())));
        }

        // When
        personalInformationPageVisit
                .enterPublicDisplayNameTo(String.format("%s final", firstName), lastName)
                .submitUpdates();

        // Then
        HalEventsPage workingPageAfterChangingNameEnoughTimesToCrossThreshold = retrieveEntrypointEventsFeedPage();
        assertThat("Expected newest page to be archived by now", previousPageUrl,
                is(not(equalTo(workingPageAfterChangingNameEnoughTimesToCrossThreshold.getLinks().getPreviousArchive().getHref()))));

        HalEventsPage previousPageAfterChangingNameEnoughTimesToCrossThreshold = httpClient()
                .get(workingPageAfterChangingNameEnoughTimesToCrossThreshold.getLinks().getPreviousArchive().getHref(), headers(acceptApplicationJsonHeader, authorizationHeader))
                .readEntity(HalEventsPage.class);
        assertThat("Expected initial name changed event to have been archived", previousPageAfterChangingNameEnoughTimesToCrossThreshold.getEvents(),
                hasItem(anEvent(forEntity(ssrnWebsite().authorForNameChangeAccountId()),
                        withDataSatisfying(hasJsonPath("$.name", is(equalTo(initialFullName)))),
                        ofType("NAME CHANGED")))
        );
    }

    private HalEventsPage retrieveEntrypointEventsFeedPage() {
        return httpClient()
                .get(ssrnAbsoluteUrl("/rest/authors/events"), headers(acceptApplicationJsonHeader, authorizationHeader))
                .readEntity(HalEventsPage.class);
    }

}
