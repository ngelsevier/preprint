package com.ssrn.papers.replicator.old_platform_contract_test.paper_entities_feed;

import com.jayway.jsonpath.JsonPath;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.golden_data.SsrnPapers.PAPER_42;
import static com.ssrn.test.support.golden_data.SsrnPapers.PAPER_45;
import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;
import static java.lang.Integer.parseInt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EntitiesFeedContract extends SsrnOldPlatformContractTest {

    private final String paperEntitiesFeedEntrypointUri = ssrnAbsoluteUrl("/rest/papers");

    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());

    public EntitiesFeedContract() {
        super(true);
    }

    @Test
    public void expectFeedPageRequestToRespondWithOKStatus() {
        // Given, When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
    }

    @Test
    public void expectFeedPageRequestToRespondWithUnauthorizedWhenNoCredentialsSent() {
        // Given, When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void expectFeedPageRequestToRespondWithUnauthorizedWhenInvalidCredentialsSent() {
        // Given
        AbstractMap.SimpleEntry<String, Object> invalidAuthorizationHeader =
                header("Authorization", base64EncodedBasicAuthorizationHeader("some", "hacker"));

        // When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, invalidAuthorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void expectEntrypointToReturnJsonMediaType() {
        // Given, When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String contentTypeHeader = response.getHeaderString("Content-Type");
        assertThat(contentTypeHeader, is(notNullValue()));

        String mediaType = contentTypeHeader.split(";")[0];
        assertThat(mediaType, is(equalTo(MediaType.APPLICATION_JSON)));

    }

    @Test
    public void expectEntrypointToReturnArrayOfEntities() {
        // Given, When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String responseBody = response.readEntity(String.class);
        assertThat(responseBody, hasJsonPath("$.papers[0].id", is(equalTo(42))));
        assertThat(responseBody, hasJsonPath("$.papers[0].title", is(equalTo(PAPER_42.getTitle()))));
        assertThat(responseBody, hasJsonPath("$.papers[0].authorIds", containsInRelativeOrder(Arrays.stream(PAPER_42.getAuthors()).map(author -> Integer.parseInt(author.getId())).toArray())));

        assertThat(responseBody, hasJsonPath("$.papers[1].id", is(equalTo(45))));
        assertThat(responseBody, hasJsonPath("$.papers[1].title", is(equalTo(PAPER_45.getTitle()))));
        assertThat(responseBody, hasJsonPath("$.papers[1].authorIds", containsInRelativeOrder(Arrays.stream(PAPER_45.getAuthors()).map(author -> Integer.parseInt(author.getId())).toArray())));

    }

    @Test
    public void expectEntitiesToBeListedInAscendingIdOrder() {
        // Given, When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String responseBody = response.readEntity(String.class);
        List<Integer> entityIds = JsonPath.read(responseBody, "$.papers[*].id");
        assertThat("Entities were not returned in ascending ID order", entityIds, is(equalTo(entityIds.stream().sorted().collect(Collectors.toList()))));
    }

    @Test
    public void expectEntitiesTobeListedInAscendingidOrderAfterGivenPaperId() {
        // Given
        int knownHistoricalPaperOneId = 48;
        int knownHistoricalPaperTwoId = 52;
        int knownHistoricalPaperThreeId = 62;
        int knownHistoricalPaperFourId = 64;
        // When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                new HashMap<String, List<Object>>() {{
                    put("afterId", Arrays.asList(knownHistoricalPaperOneId));
                }});

        // Then
        String responseBody = response.readEntity(String.class);
        List<Integer> entityIds = JsonPath.read(responseBody, "$.papers[*].id");
        List<Integer> sortedEntityIds = entityIds.stream().sorted().collect(Collectors.toList());
        assertTrue("PaperId should be greater than given ID ",
                sortedEntityIds.get(0) > knownHistoricalPaperOneId);

        assertThat("Entities were not returned in ascending ID order",
                entityIds,
                containsInRelativeOrder(knownHistoricalPaperTwoId, knownHistoricalPaperThreeId, knownHistoricalPaperFourId));
    }

    @Test
    public void expectEventEntityVersionToIncreaseConsecutivelyPerEntity() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String firstAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .getAbstractId();

        String secondAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .getAbstractId();

        ssrnWebsite().paperSubmissionPage().loadedIn(browser(), false)
                .changeTitleTo("Second Paper Title");

        MyPapersPage.Visit myPapersPageVisit = ssrnWebsite().userHomePage().visitUsing(browser())
                .sideBar()
                .myPapersLink()
                .clickWith(browser());

        myPapersPageVisit
                .editButtonForAbstract(firstAbstractId)
                .clickWith(browser())
                .acceptTermsAndConditions()
                .changeTitleTo("First Paper Title");

        // When
        Response response = httpClient().get(paperEntitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                new HashMap<String, List<Object>>() {{
                    put("afterId", Arrays.asList(Integer.parseInt(firstAbstractId) - 1));
                }});

        // Then
        String responseBody = response.readEntity(String.class);
        assertThat(responseBody, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == %d)]", firstAbstractId, 3)));
        assertThat(responseBody, hasJsonPath(String.format("$.papers[?(@.id == \"%s\" && @.version == %d)]", secondAbstractId, 3)));
    }

    @Test
    public void shouldCreateNewPapersWithIncreasingEntityIds() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        // When
        int firstAbstractId = parseInt(ssrnWebsite()
                .navigationBar()
                .submitAPaperLink().clickWith(browser())
                .acceptTermsAndConditions()
                .getAbstractId());

        // When
        int secondAbstractId = parseInt(ssrnWebsite()
                .navigationBar()
                .submitAPaperLink().clickWith(browser())
                .acceptTermsAndConditions()
                .getAbstractId());

        int thirdAbstractId = parseInt(ssrnWebsite()
                .navigationBar()
                .submitAPaperLink().clickWith(browser())
                .acceptTermsAndConditions()
                .getAbstractId());

        // Then
        assertThat(secondAbstractId, CoreMatchers.is(greaterThan(firstAbstractId)));
        assertThat(thirdAbstractId, CoreMatchers.is(greaterThan(secondAbstractId)));
    }
}
