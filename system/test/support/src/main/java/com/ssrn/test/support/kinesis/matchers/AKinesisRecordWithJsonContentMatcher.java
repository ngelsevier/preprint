package com.ssrn.test.support.kinesis.matchers;

import com.amazonaws.services.kinesis.model.Record;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class AKinesisRecordWithJsonContentMatcher extends TypeSafeMatcher<Record> {

    private final Matcher[] matchers;
    private List<Matcher> failingMatchers;

    @SafeVarargs
    public static Matcher<Record> aKinesisRecordWithContentMatching(Matcher... matchers) {
        return new AKinesisRecordWithJsonContentMatcher(matchers);
    }

    private AKinesisRecordWithJsonContentMatcher(Matcher[] matchers) {
        this.matchers = matchers;
    }

    @Override
    public void describeTo(Description description) {

        description.appendList(
                "a Kinesis record with JSON content matching: ",
                " and ",
                "",
                asList(matchers));
    }

    @Override
    protected void describeMismatchSafely(Record record, Description mismatchDescription) {
        mismatchDescription.appendText("Not a Kinesis record with expected JSON content. ");
        String recordContent = getContentIn(record);
        failingMatchers.forEach(failedMatcher -> failedMatcher.describeMismatch(recordContent, mismatchDescription));
    }

    @Override
    protected boolean matchesSafely(Record record) {
        String recordContent = getContentIn(record);

         failingMatchers = Arrays.stream(matchers)
                .filter(matcher -> !matcher.matches(recordContent))
                .collect(Collectors.toList());

        return failingMatchers.size() == 0;
    }

    private String getContentIn(Record record) {
        return new String(record.getData().array());
    }
}
