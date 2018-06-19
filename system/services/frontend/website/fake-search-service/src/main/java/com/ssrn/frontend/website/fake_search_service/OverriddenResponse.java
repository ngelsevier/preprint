package com.ssrn.frontend.website.fake_search_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;

public class OverriddenResponse {

    private final Response.Status statusCode;

    public static OverriddenResponse respondWithStatus(Response.Status created) {
        return new OverriddenResponse(created);
    }

    @JsonCreator
    public OverriddenResponse(@JsonProperty("statusCode") Response.Status statusCode) {
        this.statusCode = statusCode;
    }

    public Response.Status getStatusCode() {
        return statusCode;
    }
}
