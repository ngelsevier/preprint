package com.ssrn.authors.replicator.old_platform_contract_test.author_entities_feed;

import com.jayway.jsonpath.JsonPath;
import com.ssrn.test.support.golden_data.RealSsrnUsers;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.http.HttpClient.*;
import static java.lang.Integer.parseInt;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EntitiesFeedContract extends SsrnOldPlatformContractTest {

    private final String entitiesFeedEntrypointUri = ssrnAbsoluteUrl("/rest/authors");
    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());

    public EntitiesFeedContract() {
        super(true);
    }

    @Test
    public void expectFeedPageRequestToRespondWithOKStatus() {
        // Given, When
        Response response = httpClient().get(entitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(OK)));
    }

    @Test
    public void expectFeedPageRequestToRespondWithUnauthorizedWhenNoCredentialsSent() {
        // Given, When
        Response response = httpClient().get(entitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void expectFeedPageRequestToRespondWithUnauthorizedWhenInvalidCredentialsSent() {
        // Given
        AbstractMap.SimpleEntry<String, Object> invalidAuthorizationHeader =
                header("Authorization", base64EncodedBasicAuthorizationHeader("some", "hacker"));

        // When
        Response response = httpClient().get(entitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, invalidAuthorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void expectEntrypointToReturnJsonMediaType() {
        // Given, When
        Response response = httpClient().get(entitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String contentTypeHeader = response.getHeaderString("Content-Type");
        assertThat(contentTypeHeader, is(notNullValue()));

        String mediaType = contentTypeHeader.split(";")[0];
        assertThat(mediaType, is(equalTo(MediaType.APPLICATION_JSON)));

    }

    @Test
    public void expectEntrypointToReturnArrayOfEntities() {
        // Given, When
        Response response = httpClient().get(entitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String responseBody = response.readEntity(String.class);
        assertThat(responseBody, hasJsonPath("$.authors[0].id",is(equalTo(Integer.parseInt(RealSsrnUsers.USER_1.getId())))));
        assertThat(responseBody, hasJsonPath("$.authors[0].name",is(equalTo(RealSsrnUsers.USER_1.getPublicDisplayName()))));
        assertThat(responseBody, hasJsonPath("$.authors[1].id",is(equalTo(Integer.parseInt(RealSsrnUsers.USER_2.getId())))));
        assertThat(responseBody, hasJsonPath("$.authors[1].name",is(equalTo(RealSsrnUsers.USER_2.getPublicDisplayName()))));
    }

    @Test
    public void expectEntitiesToBeListedInAscendingIdOrder() {
        // Given, When
        Response response = httpClient().get(entitiesFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String responseBody = response.readEntity(String.class);
        List<Integer> entityIds = JsonPath.read(responseBody, "$.authors[*].id");
        assertThat("Entities were not returned in ascending ID order", entityIds, is(equalTo(entityIds.stream().sorted().collect(Collectors.toList()))));
    }

    @Test
    public void expectEntitiesTobeListedInAscendingIdOrderAfterGivenAuthorId() {
        // Given
        int knownAuthorOneId = Integer.parseInt(RealSsrnUsers.USER_47.getId());
        int knownAuthorTwoId = Integer.parseInt(RealSsrnUsers.USER_50.getId());
        int knownAuthorThreeId = Integer.parseInt(RealSsrnUsers.USER_51.getId());
        int knownAuthorFourId = Integer.parseInt(RealSsrnUsers.USER_54.getId());
        // When
        Response response = httpClient().get(entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                new HashMap<String, List<Object>>() {{
                    put("afterId", Arrays.asList(knownAuthorOneId));
                }});

        // Then
        String responseBody = response.readEntity(String.class);
        List<Integer> entityIds = JsonPath.read(responseBody, "$.authors[*].id");
        List<Integer> sortedEntityIds = entityIds.stream().sorted().collect(Collectors.toList());
        assertTrue("AuthorId should be greater than given ID ",
                sortedEntityIds.get(0) > knownAuthorOneId);

        assertThat("Entities were not returned in ascending ID order",
                entityIds,
                containsInRelativeOrder(knownAuthorTwoId, knownAuthorThreeId, knownAuthorFourId));
    }

    @Test
    public void expectEntityVersionToIncreaseConsecutivelyPerEntity() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        String firstAuthorInitialEntityFeedResponseBody = httpClient().getAndExpect(
                OK,
                entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", Integer.parseInt(ssrnWebsite().accountId()) - 1))
        ).readEntity(String.class);
        Map<String, Object> firstAuthorInitialProperties = JsonPath.read(firstAuthorInitialEntityFeedResponseBody, "$.authors[0]['id', 'version']");
        assertThat(firstAuthorInitialProperties.get("id"), is(equalTo(Integer.parseInt(ssrnWebsite().accountId()))));
        Integer firstAuthorInitialVersion = (Integer) firstAuthorInitialProperties.get("version");

        // When
        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo(String.format("Test %s", randomString()), "User 1")
                .submitUpdates();

        // Then
        String firstAuthorSubsequentEntityFeedResponseBody = httpClient().getAndExpect(
                OK,
                entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", Integer.parseInt(ssrnWebsite().accountId()) - 1))
        ).readEntity(String.class);
        Map<String, Object> firstAuthorSubsequentProperties = JsonPath.read(firstAuthorSubsequentEntityFeedResponseBody, "$.authors[0]['id', 'version']");
        assertThat(firstAuthorSubsequentProperties.get("id"), is(equalTo(Integer.parseInt(ssrnWebsite().accountId()))));
        assertThat(firstAuthorSubsequentProperties.get("version"), is(equalTo(firstAuthorInitialVersion + 1)));

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());

        String secondAuthorInitialEntityFeedResponseBody = httpClient().getAndExpect(
                OK,
                entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", Integer.parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        ).readEntity(String.class);
        Map<String, Object> secondAuthorInitialProperties = JsonPath.read(secondAuthorInitialEntityFeedResponseBody, "$.authors[0]['id', 'version']");
        assertThat(secondAuthorInitialProperties.get("id"), is(equalTo(Integer.parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()))));
        Integer secondAuthorInitialVersion = (Integer) secondAuthorInitialProperties.get("version");

        // When
        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId()).personalInfoLink())
                .enterPublicDisplayNameTo(String.format("Test %s", randomString()), "User 2")
                .submitUpdates();

        // Then
        String secondAuthorSubsequentEntityFeedResponseBody = httpClient().getAndExpect(
                OK,
                entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", Integer.parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        ).readEntity(String.class);
        Map<String, Object> secondAuthorSubsequentProperties = JsonPath.read(secondAuthorSubsequentEntityFeedResponseBody, "$.authors[0]['id', 'version']");
        assertThat(secondAuthorSubsequentProperties.get("id"), is(equalTo(Integer.parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()))));
        assertThat(secondAuthorSubsequentProperties.get("version"), is(equalTo(secondAuthorInitialVersion + 1)));
    }

    @Test
    public void expectParticipantDisplayNameChangesToBeReflectedInAuthorNameAfterRegistration() {
        // Given
        ssrnTestDataClient().ensureNoPapersAuthoredBy(ssrnWebsite().thirdAdditionalAuthorAccountId());

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().thirdAuthorAccountUsername(), ssrnWebsite().thirdAuthorAccountPassword());


        String updatedFirstName = String.format("Test %s", randomString());
        String updatedLastName = "User 2";

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().thirdAdditionalAuthorAccountId()).personalInfoLink())
                .enterPublicDisplayNameTo(updatedFirstName, updatedLastName)
                .submitUpdates();

        Response entityFeedResponseBeforeAuthorRegistered = httpClient().get(
                entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        );

        assertThat(entityFeedResponseBeforeAuthorRegistered.getStatus(), is(equalTo(OK.getStatusCode())));
        String entityFeedResponseBodyBeforeAuthorRegistered = entityFeedResponseBeforeAuthorRegistered.readEntity(String.class);
        assertThat(entityFeedResponseBodyBeforeAuthorRegistered, not(hasJsonPath(String.format("$.authors[?(@.id == \"%s\")]", ssrnWebsite().thirdAdditionalAuthorAccountId()))));

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        // When
        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .addAuthorToPaper(ssrnWebsite().thirdAdditionalAuthorEmail(), ssrnWebsite().thirdAdditionalAuthorAccountId());

        // Then
        Response entityFeedResponseAfterAuthorRegistered = httpClient().get(
                entitiesFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader),
                queryParameters(queryParameter("afterId", parseInt(ssrnWebsite().thirdAdditionalAuthorAccountId()) - 1))
        );

        assertThat(entityFeedResponseAfterAuthorRegistered.getStatus(), is(equalTo(OK.getStatusCode())));
        String entityFeedResponseBodyAfterAuthorRegistered = entityFeedResponseAfterAuthorRegistered.readEntity(String.class);
        String expectedName = String.format("%s %s", updatedFirstName, updatedLastName);
        assertThat(entityFeedResponseBodyAfterAuthorRegistered, hasJsonPath(
                String.format("$.authors[?(@.id == \"%s\" && @.name == \"%s\")]", ssrnWebsite().thirdAdditionalAuthorAccountId(), expectedName)
        ));
    }

    private static String randomString() {
        int uniqueNumber = new Random().nextInt(9999);
        return Integer.toString(uniqueNumber);
    }
}
