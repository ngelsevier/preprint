package com.ssrn.search.papers_consumer.fake_papers_service;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.logging.Level;

import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;


public class FakePaperService {
    private static final String BASE_URL = "http://fake-papers.internal-service";
    private static final int ADMIN_PORT = 81;

    private final HttpClient httpClient;

    public FakePaperService() {
        this.httpClient = createHttpClient();

        waitUntil(this::isAvailable)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(60, SECONDS);
    }

    public void hasEmittedPaperIntoKinesisStream(String streamName, PaperServicePaper paper) {
        httpClient.postAndExpect(Response.Status.OK, BASE_URL, String.format("/metadata/streams/%s/emitted-papers", streamName), paper);
    }

    public void hasEmittedUnprocessablePaperIntoKinesisStream(String streamName) {
        httpClient.postAndExpect(Response.Status.OK, BASE_URL, String.format("/metadata/streams/%s/emitted-unprocessable-papers", streamName));
    }

    public void hasEmittedPapersIntoKinesisStream(String streamName, PaperServicePaper... papers) {
        Arrays.stream(papers).forEach(paper -> hasEmittedPaperIntoKinesisStream(streamName, paper));
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
        return new HttpClient(" Fake Paper Service", new HttpClientConfiguration() {
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
