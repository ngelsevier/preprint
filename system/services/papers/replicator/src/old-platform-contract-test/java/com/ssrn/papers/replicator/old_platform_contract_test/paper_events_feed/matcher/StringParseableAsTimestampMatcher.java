package com.ssrn.papers.replicator.old_platform_contract_test.paper_events_feed.matcher;

import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class StringParseableAsTimestampMatcher extends CustomTypeSafeMatcher<String> {

    private final String dateTimeFormat;

    private StringParseableAsTimestampMatcher(String dateTimeFormat) {
        super(String.format("A date time string with format %s", dateTimeFormat));
        this.dateTimeFormat = dateTimeFormat;
    }

    public static StringParseableAsTimestampMatcher aDateTimeStringWithFormat(String dateTimeFormat) {
        return new StringParseableAsTimestampMatcher(dateTimeFormat);
    }

    @Override
    protected boolean matchesSafely(String item) {
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendPattern(dateTimeFormat)
                .toFormatter();

        try {
            DateTime.parse(item, dateTimeFormatter);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
