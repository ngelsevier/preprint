package com.ssrn.authors.replicator.http_old_platform_author_events_feed;

import com.ssrn.authors.replicator.RequestRetryLimitedExceeded;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

class PageSource {
    private static final Logger LOGGER = Logger.getLogger(PageSource.class.getName());
    private final Client httpClient;
    private final RetryConfig retryConfig;
    private final String basicAuthorizationHeader;

    PageSource(Client httpClient, String basicAuthUsername, String basicAuthPassword, int maxRequestRetries, Level httpRequestLogLevel) {
        this.httpClient = httpClient.register(new LoggingFeature(LOGGER, httpRequestLogLevel, LoggingFeature.Verbosity.HEADERS_ONLY, 8192));
        this.retryConfig = RetryConfig.custom()
                .maxAttempts(maxRequestRetries)
                .waitDuration(Duration.ofMillis(500))
                .build();
        basicAuthorizationHeader = base64EncodedBasicAuthorizationHeader(basicAuthPassword, basicAuthUsername);
    }

    Page getPageAt(String pageUrl) {
        return Try.of(getRetriableRequestWithUrl(pageUrl)).recover(throwable -> {
            LOGGER.severe(String.format("Failed To get response from pageUrl %s", pageUrl));
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

    private static String base64EncodedBasicAuthorizationHeader(String basicAuthPassword, String basicAuthUsername) {
        return String.format("Basic %s", new String(Base64.getEncoder().encode((String.format("%s:%s", basicAuthUsername, basicAuthPassword)).getBytes())));
    }
}
