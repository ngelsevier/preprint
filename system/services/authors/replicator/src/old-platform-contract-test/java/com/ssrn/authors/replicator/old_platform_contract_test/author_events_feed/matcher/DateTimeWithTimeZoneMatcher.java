package com.ssrn.authors.replicator.old_platform_contract_test.author_events_feed.matcher;

import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;

public class DateTimeWithTimeZoneMatcher extends CustomTypeSafeMatcher<ReadableInstant> {
    private final DateTimeZone expectedDateTimeZone;

    private DateTimeWithTimeZoneMatcher(DateTimeZone expectedDateTimeZone) {
        super(String.format("a date time with time zone %s", expectedDateTimeZone));
        this.expectedDateTimeZone = expectedDateTimeZone;
    }

    public static DateTimeWithTimeZoneMatcher dateWithTimeZone(DateTimeZone expectedDateTimeZone) {
        return new DateTimeWithTimeZoneMatcher(expectedDateTimeZone);
    }

    @Override
    protected boolean matchesSafely(ReadableInstant item) {
        return item.getZone().equals(expectedDateTimeZone);
    }
}
