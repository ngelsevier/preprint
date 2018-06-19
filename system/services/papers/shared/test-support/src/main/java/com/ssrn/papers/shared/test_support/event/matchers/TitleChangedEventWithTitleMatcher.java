package com.ssrn.papers.shared.test_support.event.matchers;

import com.ssrn.papers.domain.Paper;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;

public class TitleChangedEventWithTitleMatcher extends CustomTypeSafeMatcher<Paper.TitleChangedEvent> {
    private final String expectedTitle;

    public static Matcher<Paper.TitleChangedEvent> titleChangedEventTitle(String expectedTitle) {
        return new TitleChangedEventWithTitleMatcher(expectedTitle);
    }

    private TitleChangedEventWithTitleMatcher(String expectedTitle) {
        super(String.format("with title '%s'", expectedTitle));
        this.expectedTitle = expectedTitle;
    }

    @Override
    protected boolean matchesSafely(Paper.TitleChangedEvent item) {
        return expectedTitle.equals(item.getTitle());
    }
}
