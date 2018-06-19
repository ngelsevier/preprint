package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.logging.Level;

import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class FakeAuthorService {
    private static final String BASE_URL = "http://fake-authors.internal-service";
    private static final int ADMIN_PORT = 81;

    private final HttpClient httpClient;

    public FakeAuthorService() {
        this.httpClient = createHttpClient();

        waitUntil(this::isAvailable)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(60, SECONDS);
    }

    private void hasEmittedAuthorUpdateIntoKinesisStream(String streamName, AuthorServiceAuthorUpdate authorUpdate) {
        httpClient.postAndExpect(Response.Status.OK, BASE_URL, String.format("/metadata/streams/%s/emitted-author-updates", streamName), authorUpdate);
    }

    public void hasEmittedAuthorUpdatesIntoKinesisStream(String streamName, AuthorServiceAuthorUpdate... authorUpdates) {
        Arrays.stream(authorUpdates).forEach(author -> hasEmittedAuthorUpdateIntoKinesisStream(streamName, author));
    }

    public void hasEmittedUnprocessableAuthorUpdateIntoKinesisStream(String streamName) {
        httpClient.postAndExpect(Response.Status.OK, BASE_URL, String.format("/metadata/streams/%s/emitted-unprocessable-author-updates", streamName));
    }

    private Boolean isAvailable() {
        try {
            return httpClient.get(BASE_URL, "/healthcheck").getStatus() == Response.Status.OK.getStatusCode() &&
                    httpClient.get(String.format("%s:%d", BASE_URL, ADMIN_PORT), "/healthcheck").getStatus() == Response.Status.OK.getStatusCode();
        } catch (ProcessingException e) {
            return false;
        }
    }

    private HttpClient createHttpClient() {
        return new HttpClient(" Fake Author Service", new HttpClientConfiguration() {
            @Override
            public int connectionTimeoutMillisseconds() {
                return 3000;
            }

            @Override
            public int readTimeoutMilliseconds() {
                return 15000;
            }

            @Override
            public Level logLevel() {
                return Level.FINE;
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
