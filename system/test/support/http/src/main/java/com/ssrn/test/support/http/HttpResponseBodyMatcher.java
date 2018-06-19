package com.ssrn.test.support.http;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class HttpResponseBodyMatcher extends TypeSafeMatcher<InspectableResponse> {
    private final Matcher<String> responseBodyMatcher;

    public static Matcher<InspectableResponse> bodySatisfying(Matcher<String> responseBodyMatcher) {
        return new HttpResponseBodyMatcher(responseBodyMatcher);
    }

    private HttpResponseBodyMatcher(Matcher<String> responseBodyMatcher) {
        this.responseBodyMatcher = responseBodyMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a body satisfying ");
        description.appendDescriptionOf(responseBodyMatcher);
    }

    @Override
    protected boolean matchesSafely(InspectableResponse response) {
        return responseBodyMatcher.matches(response.getBody());
    }

    @Override
    protected void describeMismatchSafely(InspectableResponse item, Description mismatchDescription) {
        responseBodyMatcher.describeMismatch(item, mismatchDescription);
    }
}
