package com.ssrn.test.support.http;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;

import javax.ws.rs.core.Response;

public class HttpStatusCodeMatcher extends CustomTypeSafeMatcher<InspectableResponse> {
    private final Response.StatusType expectedStatus;

    public static CustomTypeSafeMatcher<InspectableResponse> statusCode(Response.StatusType expectedStatus) {
        return new HttpStatusCodeMatcher(expectedStatus);
    }

    private HttpStatusCodeMatcher(Response.StatusType expectedStatus) {
        super(String.format("a status code of '%s'", expectedStatus));
        this.expectedStatus = expectedStatus;
    }

    @Override
    protected boolean matchesSafely(InspectableResponse response) {
        return response.getStatusCode() == expectedStatus.getStatusCode();
    }

    @Override
    protected void describeMismatchSafely(InspectableResponse item, Description mismatchDescription) {
        mismatchDescription.appendText(String.format("was %d", item.getStatusCode()));
    }
}
