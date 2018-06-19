package com.ssrn.papers.replicator.old_platform_contract_test.paper_entities_feed;

import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformSequentialContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import com.ssrn.test.support.utils.ThreadingUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.http.HttpClient.*;
import static java.lang.Integer.parseInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaperEntitiesFeedContract extends SsrnOldPlatformSequentialContractTest {

    private static String abstractId;

    private final String paperEntitiesFeedEntrypointUri = ssrnAbsoluteUrl("/rest/papers");

    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());

    @BeforeClass
    public static void initializeFields() {
        abstractId = null;
    }

    @Test
    public void _001_expectEntityFeedToContainPaperWhenPaperSubmissionIsStarted() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        // When
        abstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .getAbstractId();

        Response entityFeedResponseAfterStartingSubmission = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterStartingSubmission.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterStartingSubmission = entityFeedResponseAfterStartingSubmission.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterStartingSubmission,
                hasJsonPath(
                        String.format("$.papers[?(@.id == \"%s\" && @.title == \"%s\" && @.version == 2 && @.isPrivate == false && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d)]",
                                abstractId, abstractId, Integer.parseInt(ssrnWebsite().accountId()))
                )
        );
    }

    @Test
    public void _002_expectEntityFeedToContainPaperWithTitleWhenPaperTitleIsChangedDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .acceptTermsAndConditions()
                .changeTitleTo("Initial Paper Title");

        // Then
        Response entityFeedResponseAfterInitialTitleChange = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterInitialTitleChange.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterInitialTitleChange = entityFeedResponseAfterInitialTitleChange.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterInitialTitleChange, hasJsonPath(String.format("$.papers[?(@.id == \"%s\")]", abstractId), hasSize(1)));
        assertThat(entityFeedResponseBodyAfterInitialTitleChange, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.title == \"%s\" && @.version == 3 && @.isPrivate == false && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d)]", abstractId, "Initial Paper Title", Integer.parseInt(ssrnWebsite().accountId()))));
    }

    @Test
    public void _003_expectEntityFeedToContainPaperIsPrivateDuringSubmission() {
        //When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .makePaperPrivate();

        // Then
        Response entityFeedResponseAfterPaperMadePrivate = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperMadePrivate.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperMadePrivate = entityFeedResponseAfterPaperMadePrivate.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperMadePrivate, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 4 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false )]", abstractId)));
    }

    @Test
    public void _004_expectEntityFeedToContainPaperWithRevisedTitleAndVersionWhenPaperTitleIsChangedDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .changeTitleTo("Revised Paper Title");

        // Then
        Response entityFeedResponseAfterInitialTitleChange = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterInitialTitleChange.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterInitialTitleChange = entityFeedResponseAfterInitialTitleChange.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterInitialTitleChange, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.title == \"%s\" && @.version == 5 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d)]", abstractId, "Revised Paper Title", Integer.parseInt(ssrnWebsite().accountId()))));

    }

    @Test
    public void _005_expectEntityFeedToContainAnAdditionalAuthorWhenAuthorIsAddedToPaperDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId());

        // Then
        Response entityFeedResponseAfterAuthorAdded = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorAdded.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterAuthorAdded = entityFeedResponseAfterAuthorAdded.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterAuthorAdded, hasJsonPath(String.format("$.papers[?(@.id == \"%s\")]", abstractId), hasSize(1)));
        assertThat(entityFeedResponseBodyAfterAuthorAdded, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 6 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d && @.authorIds[1] == %d)]", abstractId, Integer.parseInt(ssrnWebsite().accountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));

    }

    @Test
    public void _006_expectEntityFeedToContainAnUpdatedAuthorListAfterReorderingAuthorsDuringPaperSubmission() {
        // Given
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().secondAdditionalAuthorEmail(), ssrnWebsite().secondAdditionalAuthorAccountId());

        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .moveAuthorOrderForAuthor("down", ssrnWebsite().firstAdditionalAuthorAccountId());

        // When
        Response entityFeedResponseAfterAuthorReordered = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorReordered.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorReordered = entityFeedResponseAfterAuthorReordered.readEntity(String.class);

        // Then
        assertThat(entityFeedResponseBodyAfterAuthorReordered, hasJsonPath(String.format("$.papers[?(@.id == \"%s\")]", abstractId), hasSize(1)));
        assertThat(entityFeedResponseBodyAfterAuthorReordered, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 8 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d && @.authorIds[1] == %d && @.authorIds[2] == %d)]", abstractId, Integer.parseInt(ssrnWebsite().accountId()), Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));

        // Given
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .moveAuthorOrderForAuthor("up", ssrnWebsite().secondAdditionalAuthorAccountId());

        ThreadingUtils.sleepFor(1, TimeUnit.SECONDS);
        // When
        entityFeedResponseAfterAuthorReordered = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorReordered.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        entityFeedResponseBodyAfterAuthorReordered = entityFeedResponseAfterAuthorReordered.readEntity(String.class);

        // Then
        assertThat(entityFeedResponseBodyAfterAuthorReordered, hasJsonPath(String.format("$.papers[?(@.id == \"%s\")]", abstractId), hasSize(1)));
        assertThat(entityFeedResponseBodyAfterAuthorReordered, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 9 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d && @.authorIds[1] == %d && @.authorIds[2] == %d)].authorIds", abstractId, Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId()), Integer.parseInt(ssrnWebsite().accountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));

    }

    @Test
    public void _007_expectEntityFeedNotToContainTheIdOfAnAuthorRemovedDuringPaperSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .removeAuthorFromPaper(ssrnWebsite().firstAdditionalAuthorAccountId(), ssrnWebsite().accountId());

        // Then
        Response entityFeedResponseAfterAuthorAdded = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorAdded.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterAuthorRemoved = entityFeedResponseAfterAuthorAdded.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterAuthorRemoved, hasJsonPath(String.format("$.papers[?(@.id == \"%s\")]", abstractId), hasSize(1)));
        assertThat(entityFeedResponseBodyAfterAuthorRemoved, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 10 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d && @.authorIds[1] == %d)]", abstractId, Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId()), Integer.parseInt(ssrnWebsite().accountId()))));
    }

    @Test
    public void _008_expectEntityFeedNotToContainTheIdOfTheSubmitterRemovedDuringPaperSubmission(){
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId())
                .makeAuthorPrimary(ssrnWebsite().firstAdditionalAuthorAccountId())
                .removeAuthorFromPaper(ssrnWebsite().accountId(), ssrnWebsite().accountId());


        // Then
        Response entityFeedResponseAfterSubmitterRemoved = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterSubmitterRemoved.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterSubmitterRemoved = entityFeedResponseAfterSubmitterRemoved.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterSubmitterRemoved, hasJsonPath(String.format("$.papers[?(@.id == \"%s\")]", abstractId), hasSize(1)));
        assertThat(entityFeedResponseBodyAfterSubmitterRemoved, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 12 && @.isPrivate == true && @.isConsideredIrrelevant == false && @.isRestricted == false && @.authorIds[0] == %d && @.authorIds[1] == %d)]", abstractId, Integer.parseInt(ssrnWebsite().secondAdditionalAuthorAccountId()), Integer.parseInt(ssrnWebsite().firstAdditionalAuthorAccountId()))));
    }

    @Test
    public void _009_expectEntityFeedToContainPaperIsNotPrivateDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .makePaperPublic();

        // Then
        Response entityFeedResponseAfterPaperMadePublic = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperMadePublic.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperMadePrivate = entityFeedResponseAfterPaperMadePublic.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperMadePrivate, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 13 && @.isPrivate == false && @.isConsideredIrrelevant == false && @.isRestricted == false )]", abstractId)));
    }

    @Test
    public void _010_expectKeywordsChangedEventToBeEmittedWhenKeywordsAreChangedDuringSubmission() {
        // When
        ssrnWebsite()
                .paperSubmissionPage().loadedIn(browser(), false)
                .setKeywordsTo("Initial, Paper, Keywords");

        // Then
        Response entityFeedResponseAfterPaperKeywordsChanged = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperKeywordsChanged.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperKeywordsChanged = entityFeedResponseAfterPaperKeywordsChanged.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperKeywordsChanged, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 14 && @.isPrivate == false && @.isConsideredIrrelevant == false && @.isRestricted == false && @.keywords == \"Initial, Paper, Keywords\")]", abstractId)));
    }

    @Test
    public void _011_expectEntityFeedToContainPaperWithSubmissionStageSubmittedWhenPaperIsSubmitted() {
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

        // Then
        Response entityFeedResponseAfterPaperIsSubmitted = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperIsSubmitted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperIsSubmitted = entityFeedResponseAfterPaperIsSubmitted.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperIsSubmitted, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 15 && @.isRestricted == false && @.submissionStage == \"SUBMITTED\")]", abstractId)));
    }

    @Test
    public void _012_expectEntityFeedToContainPaperIsConsideredIrrelevantDuringStageOneProcessing() {

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
        Response entityFeedResponseAfterPaperConsideredIrrelevant = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperConsideredIrrelevant.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperConsideredIrrelevant = entityFeedResponseAfterPaperConsideredIrrelevant.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperConsideredIrrelevant, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 17 && @.isPrivate == false && @.isConsideredIrrelevant == true && @.isRestricted == false )]", abstractId)));
    }

    @Test
    public void _013_expectEntityFeedToContainPaperIsConsideredRelevantDuringStageOneProcessing() {
        // When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .togglePaperRelevance(abstractId, false);

        // Then
        Response entityFeedResponseAfterPaperConsideredRelevant = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperConsideredRelevant.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperConsideredRelevant = entityFeedResponseAfterPaperConsideredRelevant.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperConsideredRelevant, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 18 && @.isPrivate == false && @.isConsideredIrrelevant == false && @.isRestricted == false )]", abstractId)));
    }

    @Test
    public void _014_expectEntityFeedToContainPaperWithSubmissionStageDeletedDuringStageOneProcessing() {
        // When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "DELETED");

        // Then
        Response entityFeedResponseAfterPaperDeleted = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperDeleted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperDeleted = entityFeedResponseAfterPaperDeleted.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperDeleted, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 19 && @.isRestricted == false  && @.submissionStage == \"DELETED\")]", abstractId)));
    }

    @Test
    public void _015_expectEntityFeedToContainPaperWithSubmissionStageRejectedDuringStageOneProcessing() {
        // When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "REMOVED");

        // Then
        Response entityFeedResponseAfterPaperRejected = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperRejected.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperRejected = entityFeedResponseAfterPaperRejected.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperRejected, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 20 && @.isRestricted == false && @.submissionStage == \"REJECTED\")]", abstractId)));
    }

    @Test
    public void _016_expectEntityFeedToContainPaperWithSubmissionStageApprovedDuringStageOneProcessing() {
        // When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // Then
        Response entityFeedResponseAfterPaperApproved = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperApproved.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperRejected = entityFeedResponseAfterPaperApproved.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperRejected, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 21 && @.isRestricted == false && @.submissionStage == \"APPROVED\")]", abstractId)));
    }

    @Test
    public void _017_expectEntityFeedToContainPaperWithSubmissionStageRejectedWhenAuthorDeactivatesPaper() {
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
        Response entityFeedResponseAfterPaperDeactivated = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperDeactivated.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperDeactivated = entityFeedResponseAfterPaperDeactivated.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperDeactivated, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 22 && @.submissionStage == \"REJECTED\")]", abstractId)));
    }

    @Test
    public void _018_expectEntityFeedToContainPaperWithSubmissionStageApprovedAndIsRestrictedAttributeSetToTrueDuringStageOneProcessing() {
        // When
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
        Response entityFeedResponseAfterPaperMarkedRestricted = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperMarkedRestricted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperMarkedRestricted = entityFeedResponseAfterPaperMarkedRestricted.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperMarkedRestricted, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 24 && @.isRestricted == true && @.submissionStage == \"APPROVED\")]", abstractId)));
    }

    @Test
    public void _019_expectEntityFeedToContainPaperIsRestrictedAttributeSetToFalseDuringStageOneProcessing() {
        // When
        ssrnWebsite().stageOneClassificationPage().loadedIn(browser(), true)
                .changePaperStageTo(abstractId, "APPROVED");

        // Then
        Response entityFeedResponseAfterPaperMarkedRestricted = httpClient().get(
                paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(abstractId) - 1))
        );

        assertThat(entityFeedResponseAfterPaperMarkedRestricted.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String entityFeedResponseBodyAfterPaperMarkedRestricted = entityFeedResponseAfterPaperMarkedRestricted.readEntity(String.class);
        assertThat(entityFeedResponseBodyAfterPaperMarkedRestricted, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == 25 && @.isRestricted == false && @.submissionStage == \"APPROVED\")]", abstractId)));
    }

}
