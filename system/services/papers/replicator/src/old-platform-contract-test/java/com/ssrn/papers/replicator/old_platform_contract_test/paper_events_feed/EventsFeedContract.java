package com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed;

import com.jayway.jsonpath.JsonPath;
import com.ssrn.test.support.old_platform_contract_test.configuration.Configuration;
import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.Event;
import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.HalEventsPage;
import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.MyPapersPage;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher.DateTimeWithTimeZoneMatcher.dateWithTimeZone;
import static com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher.EventMatcher.anEvent;
import static com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher.StringParseableAsTimestampMatcher.aDateTimeStringWithFormat;
import static com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher.ValueNotNullMatcher.notNull;
import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

public class EventsFeedContract extends SsrnOldPlatformContractTest {

    private final String paperEventsFeedEntrypointUri = ssrnAbsoluteUrl("/rest/papers/events");

    private static final String EVENT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ";

    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader = header("Accept", MediaType.APPLICATION_JSON);
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader = header("Authorization", ssrnBasicAuthenticationHeader());
    private final AbstractMap.SimpleEntry<String, Object> acceptEncodingHeader = header("Accept-Encoding", "deflate, gzip");

    public EventsFeedContract() {
        super(true);
    }

    @Test
    public void expectFeedPageRequestToRespondWithOKStatus() {
        // Given, When
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
    }

    @Test
    public void expectFeedPageRequestToRespondWithUnauthorizedWhenNoCredentialsSent() {
        // Given, When
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void expectFeedPageRequestToRespondWithUnauthorizedWhenInvalidCredentialsSent() {
        // Given
        AbstractMap.SimpleEntry<String, Object> invalidAuthorizationHeader =
                header("Authorization", base64EncodedBasicAuthorizationHeader("some", "hacker"));

        // When
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader, invalidAuthorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void expectEntrypointToReturnJsonMediaType() {
        // Given, When
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String contentTypeHeader = response.getHeaderString("Content-Type");
        assertThat(contentTypeHeader, is(notNullValue()));

        String mediaType = contentTypeHeader.split(";")[0];
        assertThat(mediaType, is(equalTo(MediaType.APPLICATION_JSON)));

    }

    @Test
    public void expectEntrypointToReturnArrayOfEvents() {
        // Given, When
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String responseBody = response.readEntity(String.class);
        assertThat(responseBody, hasJsonPath("$.events", is(instanceOf(List.class))));
        assertThat(responseBody, hasJsonPath("$.events[0].id", is(instanceOf(String.class))));
        assertThat(responseBody, hasJsonPath("$.events[0].entityVersion", is(instanceOf(Integer.class))));
        assertThat(responseBody, hasJsonPath("$.events[0].entityId", is(notNull())));
        assertThat(responseBody, hasJsonPath("$.events[0].timestamp", is(aDateTimeStringWithFormat(EVENT_TIMESTAMP_FORMAT))));
        assertThat(responseBody, hasJsonPath("$.events[0].type", is(instanceOf(String.class))));

    }

    @Test
    public void expectEntrypointToSupportConditionalGetRequests() {
        // Given
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(
                acceptApplicationJsonHeader,
                authorizationHeader,
                acceptEncodingHeader)
        );

        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(response.readEntity(String.class), not(isEmptyString()));

        String eTag = response.getHeaderString("ETag");

        // When
        Response responseToIfNoneMatchRequest = httpClient().get(paperEventsFeedEntrypointUri, headers(
                acceptApplicationJsonHeader,
                authorizationHeader,
                acceptEncodingHeader,
                header("If-None-Match", eTag))
        );

        // Then
        assertThat(responseToIfNoneMatchRequest.getStatusInfo(), is(equalTo(Response.Status.NOT_MODIFIED)));
        assertThat(responseToIfNoneMatchRequest.readEntity(String.class), isEmptyString());
        assertThat(responseToIfNoneMatchRequest.getHeaderString("ETag"), is(equalTo(eTag)));

        // When
        Response responseToIfNoneMatchRequestWithDifferentEtag = httpClient().get(paperEventsFeedEntrypointUri, headers(
                acceptApplicationJsonHeader,
                authorizationHeader,
                acceptEncodingHeader,
                header("If-None-Match", "\"" + eTag.replace("\"", "") + "-different-etag\"")
        ));

        // Then
        assertThat(responseToIfNoneMatchRequestWithDifferentEtag.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(responseToIfNoneMatchRequestWithDifferentEtag.readEntity(String.class), not(isEmptyString()));
    }

    @Test
    public void expectWorkingPageCanonicalUrlToSupportConditionalGetRequests() {
        // Given
        Response entrypointResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));

        String workingPageCanonicalUrl = JsonPath.read(entrypointResponse.readEntity(String.class), "$._links.via.href");
        Response response = httpClient().get(workingPageCanonicalUrl, headers(
                acceptApplicationJsonHeader,
                authorizationHeader,
                acceptEncodingHeader));

        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(response.readEntity(String.class), not(isEmptyString()));

        String eTag = response.getHeaderString("ETag");

        // When
        Response responseToIfNoneMatchRequest = httpClient().get(workingPageCanonicalUrl, headers(
                acceptApplicationJsonHeader,
                authorizationHeader,
                acceptEncodingHeader,
                header("If-None-Match", eTag))
        );

        // Then
        assertThat(responseToIfNoneMatchRequest.getStatusInfo(), is(equalTo(Response.Status.NOT_MODIFIED)));
        assertThat(responseToIfNoneMatchRequest.readEntity(String.class), isEmptyString());
        assertThat(responseToIfNoneMatchRequest.getHeaderString("ETag"), is(equalTo(eTag)));

        // When
        Response responseToIfNoneMatchRequestWithDifferentEtag = httpClient().get(workingPageCanonicalUrl, headers(
                acceptApplicationJsonHeader,
                authorizationHeader,
                acceptEncodingHeader,
                // Quotes must surround the entire etag -> https://github.com/jersey/jersey/issues/2511
                // the original "1012803392"-different-etag is wrong, as this is treated as two separate etags, by Jersey
                header("If-None-Match", "\"" + eTag.replace("\"", "") + "-different-etag\"")
        ));

        // Then
        assertThat(responseToIfNoneMatchRequestWithDifferentEtag.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(responseToIfNoneMatchRequestWithDifferentEtag.readEntity(String.class), not(isEmptyString()));
    }

    @Test
    public void expectEntrypointPageCachedResponseToRequireRevalidationWithOriginServer() {
        // Given, When
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));

        assertThat(response.getHeaderString("Cache-Control"), is(equalTo("no-cache")));
    }

    @Test
    public void expectWorkingPageCanonicalUrlCachedResponseToRequireRevalidationWithOriginServer() {
        // Given
        Response entrypointResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String canonicalUrl = JsonPath.read(entrypointResponse.readEntity(String.class), "$._links.via.href");

        // When
        Response canonicalUrlResponse = httpClient().get(canonicalUrl, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        assertThat(canonicalUrlResponse.getHeaderString("Cache-Control"), is(equalTo("no-cache")));
    }

    @Test
    public void expectArchivedPageToBeCacheableForOneHour() {
        // Given
        Response entrypointResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String previousArchiveUrl = JsonPath.read(entrypointResponse.readEntity(String.class), "$._links.prev-archive.href");

        // When
        Response previousPageResponse = httpClient().get(previousArchiveUrl, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        assertThat(previousPageResponse.getHeaderString("Cache-Control"), is(equalTo("max-age=3600")));
    }

    @Test
    public void expectWorkingPageCanonicalUrlHasNoNextArchiveLink() {
        // Given, When
        Response entrypointResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String workingPageCanonicalUrl = JsonPath.read(entrypointResponse.readEntity(String.class), "$._links.via.href");
        Response response = httpClient().get(workingPageCanonicalUrl, headers(acceptApplicationJsonHeader, authorizationHeader));
        String responseBody = response.readEntity(String.class);

        // Then
        assertThat(responseBody, not(hasJsonPath("$._links.next-archive")));
    }

    @Test
    public void expectOldestPageToHaveNoPreviousArchiveLink() {
        // Given, When
        Response response = httpClient().get(
                paperEventsFeedEntrypointUri,
                String.format("/%s", Configuration.ssrn().firstPapersEventFeedPageId()),
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String responseBody = response.readEntity(String.class);

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(responseBody, not(hasJsonPath("$._links.prev-archive")));
    }

    @Test
    public void expectRequestForNonExistentPageToReturnNotFoundStatus() {
        // Given, When
        Response response = httpClient().get(
                paperEventsFeedEntrypointUri, "/non-existent-page",
                headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.NOT_FOUND)));
    }

    @Test
    public void expectEventsPageToListEventsOldestToNewestTopToBottom() {
        // Given
        Response response = httpClient().get(paperEventsFeedEntrypointUri, headers(acceptApplicationJsonHeader, authorizationHeader));
        HalEventsPage halEventsPage = response.readEntity(HalEventsPage.class);

        // When
        Response responseFromPreviousPage = httpClient().get(halEventsPage.getPreviousPageUrl(),
                headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        List<Event> events = responseFromPreviousPage.readEntity(HalEventsPage.class).getEvents();

        assertThatTimestampsNeverDecreaseIn(events);

        DateTime timestampOfFirstEventOnPage = timestampOf(events.get(0));
        DateTime timestampOfLastEventOnPage = timestampOf(events.get(events.size() - 1));
        assertThat(timestampOfLastEventOnPage, greaterThan(timestampOfFirstEventOnPage));
    }

    @Test
    public void expectWorkingPageCanonicalUrlToServeSameEventsAsEntrypointUrl() {
        // Given
        Response wellKnownUrlResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        HalEventsPage halEventsOnWellKnownUrl = wellKnownUrlResponse.readEntity(HalEventsPage.class);
        String canonicalUrl = halEventsOnWellKnownUrl.getLinks().getVia().getHref();

        // When
        Response canonicalUrlResponse = httpClient().get(canonicalUrl, headers(acceptApplicationJsonHeader, authorizationHeader));
        HalEventsPage halEventsOnCanonicalPage = canonicalUrlResponse.readEntity(HalEventsPage.class);

        // Then
        List<Event> eventsOnWellKnownUrl = halEventsOnWellKnownUrl.getEvents();
        List<Event> eventsOnCanonicalUrl = halEventsOnCanonicalPage.getEvents().subList(0, eventsOnWellKnownUrl.size());
        assertThat(eventsOnWellKnownUrl, is(equalTo(eventsOnCanonicalUrl)));
    }

    @Test
    public void expectEntrypointUrlAndWorkingPageCanonicalUrlResponsesToPointToSamePreviousPage() {
        // Given
        Response entrypointUrlResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String entryPointUrlPage = entrypointUrlResponse.readEntity(String.class);
        String workingPageCanonicalUrl = JsonPath.read(entryPointUrlPage, "$._links.via.href");

        // When
        Response canonicalUrlResponse = httpClient().get(workingPageCanonicalUrl, headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String canonicalUrlPage = canonicalUrlResponse.readEntity(String.class);
        String canonicalPagePreviousUrl = JsonPath.read(canonicalUrlPage, "$._links.prev-archive.href");
        assertThat(entryPointUrlPage, hasJsonPath("$._links.prev-archive.href", is(equalTo(canonicalPagePreviousUrl))));
    }

    @Test
    public void expectEntrypointUrlResponseToContainSelfLinkPointingToEntrypointUrl() {
        // Given, When
        Response entrypointUrlResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));

        // Then
        String responseBody = entrypointUrlResponse.readEntity(String.class);
        assertThat(responseBody, hasJsonPath("$._links.self.href", is(equalTo(paperEventsFeedEntrypointUri))));
    }

    @Test
    public void expectWorkingPageCanonicalUrlResponseToContainSelfLinkPointingToWorkingPageCanonicalUrl() {
        // Given
        Response entrypointUrlResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String entrypointUrlResponseBody = entrypointUrlResponse.readEntity(String.class);
        String workingPageCanonicalUrl = JsonPath.read(entrypointUrlResponseBody, "$._links.via.href");

        // When
        Response canonicalUrlResponse = httpClient().get(
                workingPageCanonicalUrl,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String canonicalUrlResponseBody = canonicalUrlResponse.readEntity(String.class);

        assertThat(canonicalUrlResponseBody, hasJsonPath("$._links.self.href", is(equalTo(workingPageCanonicalUrl))));
    }

    @Test
    public void expectWorkingPageToPointBackToNewestArchivedPageOfEvents() {
        // Given
        Response entrypoingPageResponse = httpClient().get(
                paperEventsFeedEntrypointUri,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String entrypointPageResponseBody = entrypoingPageResponse.readEntity(String.class);
        String previousArchiveUrl = JsonPath.read(entrypointPageResponseBody, "$._links.prev-archive.href");
        String entrypointCanonicalUrl = JsonPath.read(entrypointPageResponseBody, "$._links.via.href");

        // When
        Response previousArchiveUrlResponse = httpClient().get(previousArchiveUrl, headers(acceptApplicationJsonHeader, authorizationHeader));
        String previousPageResponseBody = previousArchiveUrlResponse.readEntity(String.class);

        // Then
        int previousPageLastEventIndex = (Integer) JsonPath.read(previousPageResponseBody, "$.events.length()") - 1;
        DateTime timestampOfFirstEventOnPreviousPage = parseToDate(JsonPath.read(previousPageResponseBody, "$.events[0].timestamp"));
        DateTime timestampOfLastEventOnPreviousPage = parseToDate(JsonPath.read(previousPageResponseBody, String.format("$.events[%d].timestamp", previousPageLastEventIndex)));
        assertThat(timestampOfFirstEventOnPreviousPage, is(lessThan(timestampOfLastEventOnPreviousPage)));

        DateTime timestampOfFirstEventOnEntrypointPage = parseToDate(JsonPath.read(entrypointPageResponseBody, "$.events[0].timestamp"));
        assertThat(timestampOfLastEventOnPreviousPage, is(lessThanOrEqualTo(timestampOfFirstEventOnEntrypointPage)));
        assertThat(previousPageResponseBody, hasJsonPath("$._links.next-archive.href", is(equalTo(entrypointCanonicalUrl))));

        // When
        String previousPreviousArchiveUrl = JsonPath.read(previousPageResponseBody, "$._links.prev-archive.href");
        Response previousPreviousArchiveUrlResponse = httpClient().get(previousPreviousArchiveUrl,
                headers(acceptApplicationJsonHeader, authorizationHeader));
        String previousPreviousPageResponseBody = previousPreviousArchiveUrlResponse.readEntity(String.class);

        // Then
        int previousPreviousPageLastEventIndex = (Integer) JsonPath.read(previousPreviousPageResponseBody, "$.events.length()") - 1;
        DateTime timestampOfFirstEventOnPreviousPreviousPage = parseToDate(JsonPath.read(previousPreviousPageResponseBody, "$.events[0].timestamp"));
        DateTime timestampOfLastEventOnPreviousPreviousPage = parseToDate(JsonPath.read(previousPreviousPageResponseBody, String.format("$.events[%d].timestamp", previousPreviousPageLastEventIndex)));
        assertThat(timestampOfFirstEventOnPreviousPreviousPage, is(lessThan(timestampOfLastEventOnPreviousPreviousPage)));
        assertThat(timestampOfLastEventOnPreviousPreviousPage, is(lessThanOrEqualTo(timestampOfFirstEventOnPreviousPage)));
        assertThat(previousPreviousPageResponseBody, hasJsonPath("$._links.next-archive.href", is(equalTo(previousArchiveUrl))));
    }

    @Test
    public void expectEventTimestampToBeUtcTimeOfEvent() {
        // Given
        String newestEventIdBeforePaperDrafted = ssrnApi().paperEventsStream().getNewestEvent().getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        DateTime eventTime = DateTime.now(DateTimeZone.UTC);

        // When
        String abstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .getAbstractId();

        // Then
        MatcherAssert.assertThat(() -> {
                    Event paperDraftedEvent = ssrnApi().paperEventsStream().getNewEventsAfter(newestEventIdBeforePaperDrafted, abstractId).get(0);
                    return parseToDate(paperDraftedEvent.getTimestamp());
                },
                eventuallySatisfies(CoreMatchers.is(allOf(dateWithTimeZone(DateTimeZone.UTC), greaterThan(eventTime.minusSeconds(60)), Matchers.lessThan(eventTime.plusSeconds(60)))))
                        .within(10, TimeUnit.SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void expectEventEntityVersionToIncreaseConsecutivelyPerEntity() {
        // Given
        String newestEventIdBeforePaperDrafted = ssrnApi().paperEventsStream().getNewestEvent().getId();

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
        MatcherAssert.assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(newestEventIdBeforePaperDrafted, firstAbstractId),
                // Then
                eventuallySatisfies(containsInRelativeOrder(anEvent("DRAFTED", 1), anEvent("TITLE CHANGED", 3)))
                        .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        // When
        MatcherAssert.assertThat(() -> ssrnApi().paperEventsStream().getNewEventsAfter(newestEventIdBeforePaperDrafted, secondAbstractId),
                // Then
                eventuallySatisfies(containsInRelativeOrder(anEvent("DRAFTED", 1), anEvent("TITLE CHANGED", 3)))
                        .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    private DateTime timestampOf(Event event) {
        return parseToDate(event.getTimestamp());
    }

    private static DateTime parseToDate(String dateTimeString) {
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendPattern(EVENT_TIMESTAMP_FORMAT)
                .toFormatter().withOffsetParsed();

        return DateTime.parse(dateTimeString, dateTimeFormatter);
    }

    private void assertThatTimestampsNeverDecreaseIn(List<Event> events) {
        DateTime previousEventTimestamp = new DateTime(0);

        for (Event event : events) {
            DateTime eventTimestamp = timestampOf(event);
            assertThat(eventTimestamp, greaterThanOrEqualTo(previousEventTimestamp));
            previousEventTimestamp = eventTimestamp;
        }
    }

}
