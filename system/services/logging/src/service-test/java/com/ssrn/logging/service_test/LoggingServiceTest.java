package com.ssrn.logging.service_test;

import com.ssrn.test.support.http.HttpClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.http.HttpClient.*;
import static com.ssrn.test.support.http.HttpResponseBodyMatcher.bodySatisfying;
import static com.ssrn.test.support.http.HttpResponseMatcher.anHttpResponseWith;
import static com.ssrn.test.support.http.HttpStatusCodeMatcher.statusCode;
import static com.ssrn.test.support.http.InspectableResponse.*;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

public class LoggingServiceTest {

    private static final String LOGGING_INDEX_BASE_URL = "http://logging-index.internal-service:9200";
    private static final String FAKE_SERVICE_BASE_URL = "http://fake-logging-client.internal-service";
    private static HttpClient httpClient;

    @BeforeClass
    public static void beforeClass() {
        httpClient = new HttpClient(LoggingServiceTest.class.getName());

        waitUntil(() -> receivedOkGetting(FAKE_SERVICE_BASE_URL, "/heartbeat"))
                .checkingEvery(500, MILLISECONDS)
                .forNoMoreThan(20, SECONDS);

        waitUntil(() -> receivedOkGetting(LOGGING_INDEX_BASE_URL, "/_search"))
                .checkingEvery(500, MILLISECONDS)
                .forNoMoreThan(90, SECONDS);
    }

    @Test
    public void shouldForwardFilebeatLogsToElasticsearchLoggingIndex() {
        // Given
        String uniqueString = UUID.randomUUID().toString();

        httpClient.postAndExpect(OK, FAKE_SERVICE_BASE_URL, "/logs", uniqueString);

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(LOGGING_INDEX_BASE_URL, "/_search", headers(), queryParameters(queryParameter("q", uniqueString)))),

                // Then
                eventuallySatisfies(anHttpResponseWith(allOf(
                        statusCode(OK),
                        bodySatisfying(allOf(
                                hasJsonPath("$.hits.total", is(equalTo(1))),
                                hasJsonPath("$.hits.hits[0]._source.log_message", containsString(uniqueString)),
                                hasJsonPath("$.hits.hits[0]._source.type", is(equalTo("log")))
                        ))
                ))).within(90, SECONDS, checkingEvery(500, MILLISECONDS)));
    }

    @Test
    public void shouldForwardFilebeatErrorLogsToElasticsearchLoggingIndex() {
        // Given
        String uniqueString = UUID.randomUUID().toString();

        httpClient.postAndExpect(OK, FAKE_SERVICE_BASE_URL, "/logException", uniqueString);

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(LOGGING_INDEX_BASE_URL, "/_search", headers(), queryParameters(queryParameter("q", uniqueString)))),
                // Then
                eventuallySatisfies(anHttpResponseWith(allOf(
                        statusCode(OK),
                        bodySatisfying(allOf(
                                hasJsonPath("$.hits.total", is(equalTo(1))),
                                hasJsonPath("$.hits.hits[0]._source.error_message", containsString(uniqueString)),
                                hasJsonPath("$.hits.hits[0]._source.type", is(equalTo("exception")))
                        ))
                ))).within(90, SECONDS, checkingEvery(500, MILLISECONDS)));
    }

    @Test
    public void shouldForwardFilebeatUnhandledExceptionLogsToElasticsearchLoggingIndex() {
        // Given
        String uniqueString = UUID.randomUUID().toString();

        httpClient.postAndExpect(INTERNAL_SERVER_ERROR, FAKE_SERVICE_BASE_URL, "/throwException", uniqueString);

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(LOGGING_INDEX_BASE_URL, "/_search", headers(), queryParameters(queryParameter("q", uniqueString)))),
                // Then
                eventuallySatisfies(anHttpResponseWith(allOf(
                        statusCode(OK),
                        bodySatisfying(allOf(
                                hasJsonPath("$.hits.total", is(equalTo(1))),
                                hasJsonPath("$.hits.hits[0]._source.exception_message", containsString(uniqueString)),
                                hasJsonPath("$.hits.hits[0]._source.type", is(equalTo("unhandled-exception")))
                        ))
                ))).within(90, SECONDS, checkingEvery(500, MILLISECONDS)));
    }

    private static Boolean receivedOkGetting(String baseUrl, String path) {
        try {
            return httpClient.get(baseUrl, path).getStatus() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
