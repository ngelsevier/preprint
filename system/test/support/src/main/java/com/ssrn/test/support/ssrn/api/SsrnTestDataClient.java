package com.ssrn.test.support.ssrn.api;

import com.ssrn.test.support.http.HttpClient;

import javax.ws.rs.core.Response;
import java.util.AbstractMap;

import static com.ssrn.test.support.http.HttpClient.headers;

public class SsrnTestDataClient {
    private final String authorRemovePaperAssociationUri;
    private final AbstractMap.SimpleEntry<String, Object> authorizationHeader;
    private final AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader;
    private final HttpClient httpClient;

    SsrnTestDataClient(String authorRemovePaperAssociationUri, AbstractMap.SimpleEntry<String, Object> authorizationHeader, AbstractMap.SimpleEntry<String, Object> acceptApplicationJsonHeader, HttpClient httpClient) {
        this.authorRemovePaperAssociationUri = authorRemovePaperAssociationUri;
        this.authorizationHeader = authorizationHeader;
        this.acceptApplicationJsonHeader = acceptApplicationJsonHeader;
        this.httpClient = httpClient;
    }

    public void ensureNoPapersAuthoredBy(String authorId) {
        httpClient.getAndExpect(Response.Status.OK, authorRemovePaperAssociationUri, authorId, headers(acceptApplicationJsonHeader, authorizationHeader));
    }

}
