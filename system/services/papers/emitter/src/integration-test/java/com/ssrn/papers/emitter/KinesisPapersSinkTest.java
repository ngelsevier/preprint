package com.ssrn.papers.emitter;

import com.amazonaws.services.kinesis.model.Record;
import com.ssrn.papers.domain.Paper;
import com.ssrn.test.support.kinesis.KinesisClient;
import com.ssrn.test.support.kinesis.KinesisStream;
import com.ssrn.test.support.kinesis.KinesisUtils;
import com.ssrn.test.support.logging.OverrideLogbackRootLoggerLevel;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.*;

import java.util.List;
import java.util.stream.Collectors;

import static ch.qos.logback.classic.Level.ERROR;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.papers.domain.SubmissionStage.SUBMITTED;
import static com.ssrn.papers.domain.SubmissionStage.UNDER_REVIEW;
import static com.ssrn.papers.shared.test_support.event.PaperBuilder.aPaper;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class KinesisPapersSinkTest {

    private static final String KINESIS_HOSTNAME = "localhost";
    private static final int KINESIS_PORT = 4567;
    private static final String KINESIS_STREAM_NAME = "papers-integration-tests";
    private static final int MAXIMUM_RECORD_SEQUENCE_NUMBERS_TO_CACHE = 10000;

    private static KinesisClient kinesisClient;

    @BeforeClass
    public static void createKinesisStreamHelper() {
        new KinesisStream(KINESIS_HOSTNAME, KINESIS_PORT, KINESIS_STREAM_NAME, 4);
        kinesisClient = new KinesisClient(KINESIS_HOSTNAME, KINESIS_PORT, KINESIS_STREAM_NAME, 8000, true);
    }

    @AfterClass
    public static void cleanUpKinesisStreamHelper() {
        if (kinesisClient != null) {
            kinesisClient.close();
        }
    }

    @Rule
    public OverrideLogbackRootLoggerLevel overrideLogbackRootLoggerLevel = new OverrideLogbackRootLoggerLevel(ERROR);

    @Before
    public void resetKinesisStreamHelper() {
        kinesisClient.forgetReceivedRecords();
    }

    @Test
    public void shouldWritePaperToKinesisStream() {
        // Given
        KinesisPapersSink.SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration = createSimulatedEnvironmentConfigurationFor(kinesisClient);
        KinesisPapersSink kinesisPapersSink = new KinesisPapersSink(simulatedEnvironmentConfiguration, kinesisClient.getStreamName(), true, MAXIMUM_RECORD_SEQUENCE_NUMBERS_TO_CACHE, 10, SECONDS);

        Paper privateIrrelevantRestrictedPaper = aPaper()
                .withTitle("A Paper Title")
                .withAuthorIds("1", "2", "3", "4")
                .withPaperPrivate(true)
                .withPaperIrrelevant(true)
                .withPaperRestricted(true)
                .withSubmissionStage(UNDER_REVIEW)
                .build();

        Paper publicRelevantUnrestrictedSubmittedPaper = aPaper()
                .withPaperPrivate(false)
                .withPaperIrrelevant(false)
                .withPaperRestricted(false)
                .withKeywords("any string")
                .withSubmissionStage(SUBMITTED)
                .build();

        // When
        kinesisPapersSink.streamPaper(privateIrrelevantRestrictedPaper);

        // Then
        assertThat(() -> bodiesOf(kinesisClient.records()),
                eventuallySatisfies(IsCollectionContaining.<String>hasItem(allOf(
                        hasJsonPath("$.id", is(equalTo(privateIrrelevantRestrictedPaper.getId()))),
                        hasJsonPath("$.title", is(equalTo(privateIrrelevantRestrictedPaper.getTitle()))),
                        hasJsonPath("$.authorIds", containsInAnyOrder(privateIrrelevantRestrictedPaper.getAuthorIds())),
                        hasJsonPath("$.paperPrivate", is(equalTo(Boolean.TRUE))),
                        hasJsonPath("$.paperIrrelevant", is(equalTo(Boolean.TRUE))),
                        hasJsonPath("$.paperRestricted", is(equalTo(Boolean.TRUE))),
                        hasJsonPath("$.submissionStage", is(equalTo(privateIrrelevantRestrictedPaper.getSubmissionStage().getName()))),
                        not(hasJsonPath("$.keywords"))
                ))).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));

        kinesisClient.forgetReceivedRecords();

        // When
        kinesisPapersSink.streamPaper(publicRelevantUnrestrictedSubmittedPaper);

        // Then
        assertThat(() -> bodiesOf(kinesisClient.records()),
                eventuallySatisfies(IsCollectionContaining.<String>hasItem(allOf(
                        hasJsonPath("$.id", is(equalTo(publicRelevantUnrestrictedSubmittedPaper.getId()))),
                        hasJsonPath("$.paperPrivate", is(equalTo(Boolean.FALSE))),
                        hasJsonPath("$.paperIrrelevant", is(equalTo(Boolean.FALSE))),
                        hasJsonPath("$.paperRestricted", is(equalTo(Boolean.FALSE))),
                        hasJsonPath("$.submissionStage", is(equalTo(publicRelevantUnrestrictedSubmittedPaper.getSubmissionStage().getName()))),
                        hasJsonPath("$.keywords", is(equalTo("any string")))
                ))).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldWriteUpdatesForTheSamePaperToKinesisStreamInOrderTheyWereAppended() {
        // Given
        KinesisPapersSink.SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration = createSimulatedEnvironmentConfigurationFor(kinesisClient);
        KinesisPapersSink kinesisPapersSink = new KinesisPapersSink(simulatedEnvironmentConfiguration, kinesisClient.getStreamName(), true, 10000, 10, SECONDS);

        Paper firstUpdate = aPaper().withTitle("First").build();
        String paperId = firstUpdate.getId();
        Paper secondUpdate = aPaper().withId(paperId).withTitle("Second").build();
        Paper thirdUpdate = aPaper().withId(paperId).withTitle("Third").build();
        Paper fourthUpdate = aPaper().withId(paperId).withTitle("Fourth").build();

        // When
        kinesisPapersSink.streamPaper(firstUpdate);
        kinesisPapersSink.streamPaper(secondUpdate);
        kinesisPapersSink.streamPaper(thirdUpdate);
        kinesisPapersSink.streamPaper(fourthUpdate);

        // Then
        assertThat(() -> bodiesOf(kinesisClient.records()), eventuallySatisfies(containsInRelativeOrder(
                allOf(hasJsonPath("$.id", is(equalTo(paperId))), hasJsonPath("$.title", is(equalTo(firstUpdate.getTitle())))),
                allOf(hasJsonPath("$.id", is(equalTo(paperId))), hasJsonPath("$.title", is(equalTo(secondUpdate.getTitle())))),
                allOf(hasJsonPath("$.id", is(equalTo(paperId))), hasJsonPath("$.title", is(equalTo(thirdUpdate.getTitle())))),
                allOf(hasJsonPath("$.id", is(equalTo(paperId))), hasJsonPath("$.title", is(equalTo(fourthUpdate.getTitle()))))
        )).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    private static List<String> bodiesOf(List<Record> records) {
        return records.stream().map(KinesisUtils::getKinesisRecordContent).collect(Collectors.toList());
    }

    private static KinesisPapersSink.SimulatedEnvironmentConfiguration createSimulatedEnvironmentConfigurationFor(KinesisClient kinesisClient) {
        return new KinesisPapersSink.SimulatedEnvironmentConfiguration(
                kinesisClient.getAwsRegion(),
                kinesisClient.getKinesisEndpoint(),
                kinesisClient.getAwsAccessKey(),
                kinesisClient.getAwsSecretKey()
        );
    }
}
