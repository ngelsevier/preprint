package com.ssrn.test.support.old_platform_contract_test.ssrn.api;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed.PaginatedHttpEventStream;

public class SsrnApi {

    private final String baseUrl;
    private final HttpClient httpClient;
    private String basicAuthenticationHeader;
    private PaginatedHttpEventStream paperEventsStream;
    private PaginatedHttpEventStream authorEventsStream;

    public SsrnApi(String baseUrl, HttpClient httpClient, String basicAuthenticationHeader) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.basicAuthenticationHeader = basicAuthenticationHeader;
    }

    public PaginatedHttpEventStream paperEventsStream() {
        if (paperEventsStream == null) {
            paperEventsStream = new PaginatedHttpEventStream(
                    "/rest/papers/events/",
                    baseUrl,
                    httpClient,
                    basicAuthenticationHeader
            );
        }

        return paperEventsStream;
    }

    public PaginatedHttpEventStream authorEventsStream() {
        if (authorEventsStream == null) {
            authorEventsStream = new PaginatedHttpEventStream(
                    "/rest/authors/events/",
                    baseUrl,
                    httpClient,
                    basicAuthenticationHeader
            );
        }

        return authorEventsStream;
    }
}
