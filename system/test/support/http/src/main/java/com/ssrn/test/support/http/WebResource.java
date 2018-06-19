package com.ssrn.test.support.http;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

public class WebResource {

    private final String baseUrl;

    public WebResource(String baseUrl) {
        this.baseUrl = baseUrl;

    }

    protected String absoluteUrlWithPath(String relativePath, AbstractMap.SimpleEntry<String, List<Object>>... queryParameters) {
        UriBuilder uriBuilder = UriBuilder.fromPath(baseUrl).path(relativePath);
        Arrays.stream(queryParameters).forEach(queryParameter -> uriBuilder.queryParam(queryParameter.getKey(), queryParameter.getValue().toArray()));
        return uriBuilder.toString();
    }

}
