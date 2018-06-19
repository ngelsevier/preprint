package com.ssrn.search.service_test;

import com.ssrn.search.author_updates_subscriber.fake_authors_service.FakeAuthorService;
import com.ssrn.search.papers_consumer.fake_papers_service.FakePaperService;
import com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper;
import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.search.author_updates_subscriber.fake_authors_service.AuthorServiceAuthorUpdateBuilder.anAuthorUpdate;
import static com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper.SubmissionStage.APPROVED;
import static com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper.SubmissionStage.SUBMITTED;
import static com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaperBuilder.aPaper;
import static com.ssrn.test.support.http.HttpClient.*;
import static com.ssrn.test.support.http.HttpResponseBodyMatcher.bodySatisfying;
import static com.ssrn.test.support.http.HttpResponseMatcher.anHttpResponseWith;
import static com.ssrn.test.support.http.HttpStatusCodeMatcher.statusCode;
import static com.ssrn.test.support.http.InspectableResponse.asInspectableResponse;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SearchServiceTest {

    private static final String SEARCH_API_BASE_URL = "http://search.internal-service/";

    @BeforeClass
    public static void beforeClass() {
        Client httpClient = ClientBuilder.newClient();
        waitUntil(() -> httpClient.target(SEARCH_API_BASE_URL + "healthcheck").request().get().getStatus() == OK.getStatusCode())
                .checkingEvery(500, MILLISECONDS)
                .forNoMoreThan(90, SECONDS);
    }

    @Test
    public void shouldProvidePapersAndAuthorsWhoseAuthorNameWasMatchedBySearchQuery() {
        // Given
        FakePaperService fakePaperService = new FakePaperService();

        String firstPaperId = getRandomString();
        String secondPaperId = getRandomString();
        String thirdPaperId = getRandomString();
        String uniqueTitle = getRandomString();
        String anotherUniqueTitle = getRandomString();

        FakeAuthorService fakeAuthorService = new FakeAuthorService();
        fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream("author-updates",
                anAuthorUpdate()
                        .withId("99")
                        .withName("Sam Smith")
                        .build()
        );

        fakePaperService.hasEmittedPapersIntoKinesisStream("papers",
                aPaper()
                        .withId(firstPaperId)
                        .withTitle(format("First Paper %s Title", uniqueTitle))
                        .withAuthorIds("110", "102")
                        .withSubmissionStage(SUBMITTED)
                        .build(),
                aPaper()
                        .withId(secondPaperId)
                        .withTitle(format("Second Paper %s Title", uniqueTitle))
                        .withAuthorIds("102", "106")
                        .withSubmissionStage(APPROVED)
                        .build(),
                aPaper()
                        .withId(thirdPaperId)
                        .withTitle(format("Third Paper %s Title", anotherUniqueTitle))
                        .withKeywords("first, paper, keywords")
                        .withAuthorIds("106")
                        .withSubmissionStage(SUBMITTED)
                        .build()
        );

        HttpClient client = createHttpClient();
        checkUntilSearchApiReady(client, 15, SECONDS);

        // Guard assertion to ensure papers can be found in eventually-consistent papers index before publishing author updates
        assertThat(() -> asInspectableResponse(client.get(SEARCH_API_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", uniqueTitle),
                        queryParameter("from", 0),
                        queryParameter("size", 50)
                ))),
                eventuallySatisfies(
                        anHttpResponseWith(
                                both(statusCode(OK))
                                        .and(bodySatisfying(allOf(
                                                hasJsonPath(String.format("$.results[?(@.id == '%s')]", firstPaperId)),
                                                hasJsonPath(String.format("$.results[?(@.id == '%s')]", secondPaperId))
                                        )))
                        )
                ).within(15, SECONDS, checkingEvery(100, MILLISECONDS))
        );


        String uniqueName = getRandomString();

        fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream("author-updates",
                anAuthorUpdate()
                        .withId("110")
                        .withName("Sammy Jo")
                        .build(),
                anAuthorUpdate()
                        .withId("102")
                        .withName("Billy Bob")
                        .build(),
                anAuthorUpdate()
                        .withId("106")
                        .withName(uniqueName)
                        .build()
        );

        // When, Then
        assertThat(() -> asInspectableResponse(client.get(SEARCH_API_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", uniqueTitle),
                        queryParameter("from", 0),
                        queryParameter("size", 50)
                ))),
                eventuallySatisfies(
                        anHttpResponseWith(
                                allOf(statusCode(OK),
                                        bodySatisfying(allOf(
                                                hasJsonPath(String.format("$.results[?(@.@type == 'Paper' && @.id == '%s')]", firstPaperId)),
                                                hasJsonPath(String.format("$.results[?(@.@type == 'Paper' && @.id == '%s')]", secondPaperId))
                                        )),
                                        not(bodySatisfying(allOf(
                                                hasJsonPath(String.format("$.results[?(@.@type == 'Paper' && @.id == '%s')]", thirdPaperId))
                                        )))
                                )
                        )
                ).within(15, SECONDS, checkingEvery(100, MILLISECONDS)));


        // When, Then
        String expectedHighlightedUniqueName = String.format("<em>%s</em>", uniqueName);

        assertThat(() -> asInspectableResponse(client.get(SEARCH_API_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", uniqueName),
                        queryParameter("from", 0),
                        queryParameter("size", 50)
                ))),
                eventuallySatisfies(
                        anHttpResponseWith(
                                both(statusCode(OK))
                                        .and(bodySatisfying(allOf(
                                                hasJsonPath(String.format(
                                                        "$.results[?(" +
                                                                "@.@type == 'Author' && " +
                                                                "@.id == '106' && " +
                                                                "@.name == '%s'" + ")]",
                                                        expectedHighlightedUniqueName
                                                )),
                                                hasJsonPath(String.format(
                                                        "$.results[?(" +
                                                                "@.@type == 'Paper' && " +
                                                                "@.id == '%s' && @.title == 'Third Paper %s Title' && " +
                                                                "@.keywords == 'first, paper, keywords' && " +
                                                                "@.authors[0].id == '106' && @.authors[0].name == '%s'" +
                                                                ")]",
                                                        thirdPaperId,
                                                        anotherUniqueTitle,
                                                        expectedHighlightedUniqueName
                                                )),
                                                hasJsonPath(String.format(
                                                        "$.results[?(" +
                                                                "@.@type == 'Paper' && " +
                                                                "@.id == '%s' && @.title == 'Second Paper %s Title' && " +
                                                                "@.authors[0].id == '102' && @.authors[0].name == 'Billy Bob' && " +
                                                                "@.authors[1].id == '106' && @.authors[1].name == '%s'" +
                                                                ")]",
                                                        secondPaperId,
                                                        uniqueTitle,
                                                        expectedHighlightedUniqueName
                                                )),
                                                not(hasJsonPath(String.format("$.results[?(@.id == '%s')]", firstPaperId)))
                                        )))
                        )
                ).within(15, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldProvideSearchResultsAccordingToPaginationRequested() {
        // Given
        FakePaperService fakePaperService = new FakePaperService();

        String firstPaperId = getRandomString();
        String secondPaperId = getRandomString();
        String thirdPaperId = getRandomString();
        String uniqueTitle = getRandomString();

        fakePaperService.hasEmittedPapersIntoKinesisStream("papers",
                aPaper()
                        .withId(firstPaperId)
                        .withTitle(format("First Paper %s Title", uniqueTitle))
                        .withAuthorIds("110", "102")
                        .withSubmissionStage(SUBMITTED)
                        .build(),
                aPaper()
                        .withId(secondPaperId)
                        .withTitle(format("Second Paper %s Title", uniqueTitle))
                        .withAuthorIds("102", "106")
                        .withSubmissionStage(APPROVED)
                        .build(),
                aPaper()
                        .withId(thirdPaperId)
                        .withTitle(format("Third Paper %s Title", uniqueTitle))
                        .withKeywords("first, paper, keywords")
                        .withAuthorIds("106")
                        .withSubmissionStage(SUBMITTED)
                        .build()
        );

        HttpClient client = createHttpClient();
        checkUntilSearchApiReady(client, 15, SECONDS);

        assertThat(
                // When
                () -> asInspectableResponse(client.get(SEARCH_API_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", uniqueTitle),
                        queryParameter("from", 0),
                        queryParameter("size", 1)
                ))),

                // Then
                eventuallySatisfies(
                        anHttpResponseWith(
                                both(statusCode(OK))
                                        .and(bodySatisfying(allOf(
                                                hasJsonPath("$['totalNumberOfResults']", is(3)),
                                                hasJsonPath("$.results.length()", is(equalTo(1)))
                                        )))
                        )
                ).within(15, SECONDS, checkingEvery(100, MILLISECONDS))
        );


        assertThat(
                // When
                () -> asInspectableResponse(client.get(SEARCH_API_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", uniqueTitle),
                        queryParameter("from", 1),
                        queryParameter("size", 2)
                ))),

                // Then
                eventuallySatisfies(
                        anHttpResponseWith(
                                both(statusCode(OK))
                                        .and(bodySatisfying(allOf(
                                                hasJsonPath("$['totalNumberOfResults']", is(3)),
                                                hasJsonPath("$.results.length()", is(equalTo(2)))
                                        )))
                        )
                ).within(15, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void checkUntilSearchApiReady(HttpClient client, int timeout, TimeUnit timeoutUnit) {
        long startTime = System.currentTimeMillis();
        long timeoutInMilliseconds = timeoutUnit.toMillis(timeout);
        boolean stopChecking = false;
        String searchString = getRandomString();

        while (!stopChecking && System.currentTimeMillis() - startTime < timeoutInMilliseconds) {
            try {
                Response response = client.get(SEARCH_API_BASE_URL, headers(), queryParameters(queryParameter("query", searchString)));
                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    stopChecking = true;
                }
            } catch (Throwable throwable) {
            }
        }

        if (!stopChecking) {
            throw new RuntimeException(String.format("Search API is still not ready after %d %s", timeout, timeoutUnit.toString()));
        }
    }

    private static HttpClient createHttpClient() {
        return new HttpClient("HTTP Client", new HttpClientConfiguration() {
            @Override
            public int connectionTimeoutMillisseconds() {
                return 500;
            }

            @Override
            public int readTimeoutMilliseconds() {
                return 500;
            }

            @Override
            public Level logLevel() {
                return Level.INFO;
            }

            @Override
            public Boolean logEntity() {
                return false;
            }

            @Override
            public int maxEntityBytesToLog() {
                return 0;
            }
        });
    }

}
