package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OverriddenResponse {
    private final String contentType;
    private final int statusCode;
    private final String body;

    @JsonCreator
    public OverriddenResponse(@JsonProperty("statusCode") int statusCode, @JsonProperty("contentType") String contentType, @JsonProperty("body") String body) {
        this.contentType = contentType;
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }
}
