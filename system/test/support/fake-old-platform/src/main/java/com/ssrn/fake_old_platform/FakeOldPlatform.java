package com.ssrn.fake_old_platform;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.ssrn.test.support.http.HttpClient.*;

public class FakeOldPlatform {

    private static final String BASE_URL = "http://localhq.ssrn.com";
    private final HttpClient client;

    public FakeOldPlatform() {
        client = createHttpClient();
    }

    private static HttpClient createHttpClient() {
        return new HttpClient("Fake Old Platform", new HttpClientConfiguration() {
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

    public PaperEvent[] getAllEventsForPaper(String abstractId) {
        return client.get(BASE_URL, String.format("/metadata/papers/%s/events", abstractId)).readEntity(PaperEvent[].class);
    }

    public void delaysNextResponseBy(long delay, TimeUnit delayUnit) {
        client.putAndExpect(
                Response.Status.OK,
                BASE_URL,
                "/metadata/next-response-millisecond-delay",
                Entity.text(delayUnit.toMillis(delay))
        );
    }

    public Paper hasPaperThatWasCreatedBeforeEventFeedExisted(String paperTitle, String keywords, String[] authorIds, boolean isPaperPrivate, boolean isPaperIrrelevant, boolean isPaperRestricted, String submissionStage) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", paperTitle);
        data.put("keywords", keywords);
        data.put("authorIds", authorIds);
        data.put("paperPrivate", isPaperPrivate);
        data.put("paperIrrelevant", isPaperIrrelevant);
        data.put("paperRestricted", isPaperRestricted);
        data.put("submissionStage", submissionStage);
        return client.postAndExpect(Response.Status.CREATED, BASE_URL, "/metadata/add-historical-paper", data, Paper.class);
    }

    public Integer hasAuthorThatWasCreatedBeforeEventFeedExisted(String authorName) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", authorName);
        return client.postAndExpect(Response.Status.CREATED, BASE_URL, "/metadata/add-historical-author", data, Integer.class);
    }

    public Integer getNextAbstractId() {
        return client.get(BASE_URL, "/metadata/next-abstract-id").readEntity(Integer.class);
    }

    public int getNumberOfPapersInEntityFeedAfterPaperId(String paperId) {
        return client.get(BASE_URL, "/metadata/number-of-papers-in-entity-feed-after-paper", headers(), queryParameters(queryParameter("paperId", paperId))).readEntity(Integer.class);
    }

    public void nextResponseWillBe(int statusCode, String contentType, String body) {
        client.postAndExpect(Response.Status.CREATED, BASE_URL, "/metadata/overridden-responses", new OverriddenResponse(statusCode, contentType, body));
    }

    public void resetOverrides() {
        client.postAndExpect(Response.Status.OK, BASE_URL, "/metadata/reset-overrides");
    }

    public int getNumberOfAuthorsInEntityFeedAfterAuthorId(String authorId) {
        return client.get(BASE_URL, "/metadata/number-of-authors-in-entity-feed-after-author", headers(), queryParameters(queryParameter("authorId", authorId))).readEntity(Integer.class);
    }

    public Integer getNextAuthorId() {
        return client.get(BASE_URL, "/metadata/next-author-id").readEntity(Integer.class);
    }

}
