package com.ssrn.papers.replicator.http_old_platform_paper_entities_feed;

import com.ssrn.papers.replicator.RequestRetryLimitedExceeded;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

class PageSource {
    private static final Logger LOGGER = Logger.getLogger(PageSource.class.getName());
    private static final String ENTITIES_FEED_PATH = "/rest/papers";

    private final Client httpClient;
    private final String baseUrl;
    private final RetryConfig retryConfig;
    private final String basicAuthorizationHeader;

    PageSource(Client httpClient, String baseUrl, String basicAuthUsername, String basicAuthPassword, int maxRequestRetries, Level httpRequestLogLevel) {
        this.httpClient = httpClient.register(new LoggingFeature(LOGGER, httpRequestLogLevel, LoggingFeature.Verbosity.HEADERS_ONLY, 8192));
        this.baseUrl = baseUrl;
        this.retryConfig = RetryConfig.custom()
                .maxAttempts(maxRequestRetries)
                .waitDuration(Duration.ofMillis(500))
                .build();
        basicAuthorizationHeader = base64EncodedBasicAuthorizationHeader(basicAuthUsername, basicAuthPassword);
    }

    Page getEntrypointPage() {
        return getPageStartingAfterPaperId(null);
    }

    Page getPageStartingAfterPaperId(Integer paperId) {
        UriBuilder uri = UriBuilder.fromPath(baseUrl)
                .path(ENTITIES_FEED_PATH);

        if (paperId != null) {
            uri.queryParam("afterId", paperId);
        }

        return Try.of(getRetriableRequestWithUrl(uri.toString())).recover(throwable -> {
            throw new RequestRetryLimitedExceeded(throwable);
        }).get();
    }

    private CheckedFunction0<Page> getRetriableRequestWithUrl(String pageUrl) {
        return Retry.decorateCheckedSupplier(Retry.of("id", retryConfig), () -> httpClient
                .target(pageUrl)
                .request()
                .header("Accept", MediaType.APPLICATION_JSON)
                .header("Authorization", basicAuthorizationHeader)
                .get()
                .readEntity(Page.class));
    }

    private static String base64EncodedBasicAuthorizationHeader(String basicAuthUsername, String basicAuthPassword) {
        String basicAuthUsernamePasswordString = String.format("%s:%s", basicAuthUsername, basicAuthPassword);
        String basicAuthValue = new String(Base64.getEncoder().encode(basicAuthUsernamePasswordString.getBytes()));

        return String.format("Basic %s", basicAuthValue);
    }
}
