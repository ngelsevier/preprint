package com.ssrn.authors.replicator.old_platform_contract_test.author_entities_feed;

import com.jayway.jsonpath.JsonPath;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformSequentialContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Random;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.http.HttpClient.*;
import static java.lang.Integer.parseInt;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorEntitiesFeedContract extends SsrnOldPlatformSequentialContractTest {

    private final String authorEntitiesFeedEntrypointUri = ssrnAbsoluteUrl("/rest/authors");
    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());

    private static Integer version;
    private static String abstractId;

    @Test
    public void _001_expectThatWeCanChangeParticipantDisplayNameOfAnAuthor() {
        // Given
        String firstName = String.format("Test %s", randomString());
        String lastName = "User 1";

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions();

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo(firstName, lastName)
                .submitUpdates();

        // When
        Response entityFeedResponseAfterAuthorNameUpdate = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorNameUpdate.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorNameUpdate = entityFeedResponseAfterAuthorNameUpdate.readEntity(String.class);
        version = JsonPath.read(entityFeedResponseBodyAfterAuthorNameUpdate, "$.authors[0].version");

        // Then
        String expectedName = String.format("%s %s", firstName, lastName);
        assertThat(entityFeedResponseBodyAfterAuthorNameUpdate, hasJsonPath(String.format("$.authors[?(@.id == \"%s\")].name", ssrnWebsite().accountId()), hasItem(expectedName)));
    }

    @Test
    public void _002_expectThatEntityVersionIsIncreasedAfterChangeOfAuthorName() {
        // Given
        String firstName = String.format("Test %s", randomString());
        String lastName = "User 1";
        String fullName = String.format("%s %s", firstName, lastName);

        ssrnWebsite().personalInformationPage(ssrnWebsite().accountId()).loadedIn(browser(), true).enterPublicDisplayNameTo(firstName, lastName).submitUpdates();

        // When
        Response entityFeedResponseAfterAuthorNameUpdate = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorNameUpdate.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorNameUpdate = entityFeedResponseAfterAuthorNameUpdate.readEntity(String.class);

        // Then
        assertThat(entityFeedResponseBodyAfterAuthorNameUpdate,
                hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]['name','version']", ssrnWebsite().accountId()), hasItem(both(hasEntry("version", version + 1)).and(hasEntry("name", fullName))))
        );
    }

    @Test
    public void _003_expectAnNonExistingAuthorToAppearInEntityFeedAfterBeingAddedAsAuthorDuringPaperSubmission() {
        ssrnTestDataClient().ensureNoPapersAuthoredBy(ssrnWebsite().thirdAdditionalAuthorAccountId());

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());

        String firstName = String.format("Billy %s", randomString());
        String lastName = "Bob";

        ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId())
                .personalInfoLink()
                .clickWith(browser())
                .enterPublicDisplayNameTo(firstName, lastName)
                .submitUpdates();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        PaperSubmissionPage.Visit submissionPageVisit = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        submissionPageVisit
                .acceptTermsAndConditions()
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        // When
        Response entityFeedResponseAfterAuthorAdd = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorAdd.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorAdd = entityFeedResponseAfterAuthorAdd.readEntity(String.class);

        // Then
        assertThat(entityFeedResponseBodyAfterAuthorAdd, hasJsonPath(String.format("$.authors[?(@.id == \"%s\")].name", ssrnWebsite().thirdAdditionalAuthorAccountId()), hasItem(String.format("%s %s", firstName, lastName))));
    }

    @Test
    public void _004_expectExistingAuthorRemovedFromEntityFeedAfterBeingRemovedAsAuthorDuringPaperSubmission() {
        // Given
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .removeAuthorFromPaper(ssrnWebsite().thirdAdditionalAuthorAccountId(), ssrnWebsite().accountId());

        // When
        Response entityFeedResponseAfterAuthorRemove = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorRemove.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorRemove = entityFeedResponseAfterAuthorRemove.readEntity(String.class);

        // Then
        assertThat(entityFeedResponseBodyAfterAuthorRemove, not(hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().thirdAdditionalAuthorAccountId()))));

    }

    @Test
    public void _005_expectNoAuthorEntityFeedEntryForNonAuthorAfterNameChange() {
        // Given
        String updatedFirstName = String.format("%s %s", ssrnWebsite().thirdAdditionalAuthorName().split(" ")[0], randomString());
        String lastName = ssrnWebsite().thirdAdditionalAuthorName().substring(ssrnWebsite().thirdAdditionalAuthorName().indexOf(" ") + 1);

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId()).personalInfoLink())
                .enterPublicDisplayNameTo(updatedFirstName, lastName)
                .submitUpdates();

        ssrnWebsite().personalInformationPage(ssrnWebsite().thirdAdditionalAuthorAccountId()).loadedIn(browser(), true)
                .enterPublicDisplayNameTo(ssrnWebsite().thirdAdditionalAuthorName().split(" ")[0], lastName)
                .submitUpdates();

        // When
        Response entityFeedResponseAfterAuthorNameUpdate = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorNameUpdate.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorNameUpdate = entityFeedResponseAfterAuthorNameUpdate.readEntity(String.class);

        // Then
        assertThat(entityFeedResponseBodyAfterAuthorNameUpdate, not(hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().thirdAdditionalAuthorAccountId()))));
    }

    @Ignore("WIP-316")
    @Test
    public void _006_expectAnExistingAuthorToDisappearInEntityFeedAfterHisOnlyPaperHasBeenDeleted() {
        // Given
        ssrnTestDataClient().ensureNoPapersAuthoredBy(ssrnWebsite().accountId());

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        PaperSubmissionPage.Visit paperSubmissionPageVisit = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        abstractId = paperSubmissionPageVisit.getAbstractId();

        // When
        Response entityFeedResponseAfterPaperSubmission = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        // Then
        assertThat(entityFeedResponseAfterPaperSubmission.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterPaperSubmission = entityFeedResponseAfterPaperSubmission.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperSubmission, hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().accountId())));

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
        Response entityFeedResponseAfterPaperMadeRestricted = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        // Then
        assertThat(entityFeedResponseAfterPaperMadeRestricted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterPaperMadeRestricted = entityFeedResponseAfterPaperMadeRestricted.readEntity(String.class);

        assertThat(entityFeedResponseBodyAfterPaperMadeRestricted, not(hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().accountId()))));
    }

    @Ignore("WIP-316")
    @Test
    public void _007_expectAnNonAuthorToAppearInEntityFeedAfterHisOnlyPaperHasBeenApproved() {
        // Given
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // When
        Response entityFeedResponseAfterPaperApproved = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        // Then
        assertThat(entityFeedResponseAfterPaperApproved.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterPaperApproved = entityFeedResponseAfterPaperApproved.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperApproved, hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().accountId())));
    }

    @Ignore("WIP-316")
    @Test
    public void _008_expectAnExistingAuthorToDisappearInEntityFeedAfterHisOnlyPaperHasBeenDeactivated() {
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
        Response entityFeedResponseAfterPaperDeactivated = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        // Then
        assertThat(entityFeedResponseAfterPaperDeactivated.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterPaperDeactivated = entityFeedResponseAfterPaperDeactivated.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperDeactivated, not(hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().accountId()))));
    }

    @Ignore("WIP-316")
    @Test
    public void _009_expectAnNonAuthorToAppearInEntityFeedAfterHisOnlyPaperHasBeenApproved() {
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
        Response entityFeedResponseAfterPaperApprovedRestricted = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        // Then
        assertThat(entityFeedResponseAfterPaperApprovedRestricted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterPaperApprovedRestricted = entityFeedResponseAfterPaperApprovedRestricted.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperApprovedRestricted, hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().accountId())));
    }

    @Ignore("WIP-316")
    @Test
    public void _010_expectAnNonAuthorToAppearInEntityFeedAfterHisOnlyPaperHasBeenRejected() {
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
        Response entityFeedResponseAfterPaperApprovedRestricted = httpClient().get(
                authorEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().accountId()) - 1))
        );

        // Then
        assertThat(entityFeedResponseAfterPaperApprovedRestricted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterPaperApprovedRestricted = entityFeedResponseAfterPaperApprovedRestricted.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperApprovedRestricted, not(hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().accountId()))));
    }
    private static String randomString() {
        int uniqueNumber = new Random().nextInt(9999);
        return Integer.toString(uniqueNumber);
    }
}