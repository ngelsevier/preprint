package com.ssrn.test.support.http;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class HttpResponseMatcher extends TypeSafeMatcher<InspectableResponse> {
    private final Matcher<InspectableResponse> responseMatcher;

    public static TypeSafeMatcher<InspectableResponse> anHttpResponseWith(Matcher<InspectableResponse> responseMatcher) {
        return new HttpResponseMatcher(responseMatcher);
    }

    private HttpResponseMatcher(Matcher<InspectableResponse> responseMatcher) {
        this.responseMatcher = responseMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an HTTP response with ");
        description.appendDescriptionOf(responseMatcher);
    }

    @Override
    protected void describeMismatchSafely(InspectableResponse item, Description mismatchDescription) {
        responseMatcher.describeMismatch(item, mismatchDescription);
    }

    @Override
    protected boolean matchesSafely(InspectableResponse item) {
        return responseMatcher.matches(item);
    }
}
