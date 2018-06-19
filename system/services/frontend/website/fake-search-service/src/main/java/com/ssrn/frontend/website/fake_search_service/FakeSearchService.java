package com.ssrn.frontend.website.fake_search_service;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.logging.Level;

import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

public class FakeSearchService {
    public static final String BASE_URL = "http://fake-search.internal-service";

    private final String baseUrl;

    private final HttpClient httpClient;

    public FakeSearchService() {
        this.httpClient = createHttpClient();
        this.baseUrl = BASE_URL;

        waitUntil(this::isAvailable)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(60, SECONDS);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void nextResponse(OverriddenResponse overriddenResponse) {
        httpClient.postAndExpect(CREATED, baseUrl, "/metadata/overridden-responses", overriddenResponse);
    }

    public void reset() {
        httpClient.postAndExpect(OK, baseUrl, "/metadata/reset");
    }

    public void has(IndexedPaperBuilder indexedPaperBuilder) {
        httpClient.postAndExpect(CREATED, baseUrl, "/metadata/indexed-papers", indexedPaperBuilder.build());
    }

    public void has(IndexedPaperBuilder... indexedPaperBuilders) {
        Arrays.stream(indexedPaperBuilders).forEach(this::has);
    }

    public void has(IndexedAuthorBuilder indexedAuthorBuilder) {
        httpClient.postAndExpect(CREATED, baseUrl, "/metadata/indexed-authors", indexedAuthorBuilder.build());
    }

    public void has(IndexedAuthorBuilder... indexedAuthorBuilders) {
        Arrays.stream(indexedAuthorBuilders).forEach(this::has);
    }

    private Boolean isAvailable() {
        try {
            return httpClient.get(baseUrl, "/healthcheck").getStatus() == Response.Status.OK.getStatusCode();
        } catch (ProcessingException e) {
            return false;
        }
    }

    private static HttpClient createHttpClient() {
        return new HttpClient("Fake Search Api", new HttpClientConfiguration() {
            @Override
            public int connectionTimeoutMillisseconds() {
                return 3000;
            }

            @Override
            public int readTimeoutMilliseconds() {
                return 3000;
            }

            @Override
            public Level logLevel() {
                return Level.INFO;
            }

            @Override
            public Boolean logEntity() {
                return true;
            }

            @Override
            public int maxEntityBytesToLog() {
                return 4096;
            }
        });
    }

}
