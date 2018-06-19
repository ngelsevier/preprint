package com.ssrn.test.support.http;

import javax.ws.rs.core.Response;

public class InspectableResponse {
    private final Response response;
    private String body;

    public static InspectableResponse asInspectableResponse(Response response) {
        return new InspectableResponse(response);
    }

    private InspectableResponse(Response response) {
        this.response = response;
    }

    public int getStatusCode() {
        return response.getStatusInfo().getStatusCode();
    }

    public String getBody() {
        if (body == null) {
            body = response.readEntity(String.class);
        }

        return body;
    }

    @Override
    public String toString() {
        return String.format("an HTTP response with status %d %s and body: %s", response.getStatus(), response.getStatusInfo(), getBody());
    }
}
