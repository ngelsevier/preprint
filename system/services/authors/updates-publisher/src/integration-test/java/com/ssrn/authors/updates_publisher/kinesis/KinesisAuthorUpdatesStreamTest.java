package com.ssrn.authors.updates_publisher.kinesis;

import com.amazonaws.services.kinesis.model.Record;
import com.ssrn.authors.domain.AuthorUpdate;
import com.ssrn.authors.updates_publisher.KinesisAuthorUpdatesStream;
import com.ssrn.test.support.kinesis.KinesisClient;
import com.ssrn.test.support.kinesis.KinesisStream;
import com.ssrn.test.support.kinesis.KinesisUtils;
import com.ssrn.test.support.logging.OverrideLogbackRootLoggerLevel;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ch.qos.logback.classic.Level.ERROR;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.authors.shared.test_support.entity.AuthorBuilder.anAuthor;
import static com.ssrn.authors.shared.test_support.entity.AuthorUpdateBuilder.anAuthorUpdate;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class KinesisAuthorUpdatesStreamTest {

    private static final String KINESIS_HOSTNAME = "localhost";
    private static final int KINESIS_PORT = 4567;
    private static final String KINESIS_STREAM_NAME = "author-updates-integration-tests";
    private static final int MAXIMUM_RECORD_SEQUENCE_NUMBERS_TO_CACHE = 10000;

    @Rule
    public OverrideLogbackRootLoggerLevel overrideLogbackRootLoggerLevel = new OverrideLogbackRootLoggerLevel(ERROR);

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

    @Before
    public void resetKinesisStreamHelper() {
        kinesisClient.forgetReceivedRecords();
    }

    @Test
    public void shouldPublishAuthorUpdateToKinesisStream() {
        // Given
        KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration = createSimulatedEnvironmentConfigurationFor(kinesisClient);
        KinesisAuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(simulatedEnvironmentConfiguration, kinesisClient.getStreamName(), true, MAXIMUM_RECORD_SEQUENCE_NUMBERS_TO_CACHE, 10, SECONDS);

        AuthorUpdate authorUpdate = anAuthorUpdate()
                .withId("123")
                .withAuthor(anAuthor().withId("123").withName("John Doe").withRemoval(true))
                .build();
        AuthorUpdate authorUpdate2 = anAuthorUpdate()
                .withId("234")
                .withAuthor(anAuthor().withId("234").withName("John Major").withRemoval(false))
                .build();

        // When
        kinesisAuthorUpdatesStream.publish(authorUpdate);
        kinesisAuthorUpdatesStream.publish(authorUpdate2);

        // Then
        assertThat(() -> bodiesOf(kinesisClient.records()),
                eventuallySatisfies(IsCollectionContaining.<String>hasItem(
                        allOf(
                                hasJsonPath("$.id", is(equalTo("123"))),
                                hasJsonPath("$.author.id", is(equalTo("123"))),
                                hasJsonPath("$.author.name", is(equalTo("John Doe"))),
                                hasJsonPath("$.author.removed", is(equalTo(true)))
                        ))
                ).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));

        assertThat(() -> bodiesOf(kinesisClient.records()),
                eventuallySatisfies(IsCollectionContaining.<String>hasItem(
                        allOf(
                                hasJsonPath("$.id", is(equalTo("234"))),
                                hasJsonPath("$.author.id", is(equalTo("234"))),
                                hasJsonPath("$.author.name", is(equalTo("John Major"))),
                                hasJsonPath("$.author.removed", is(equalTo(false)))
                        ))
                ).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldPublishUpdatesForTheSameAuthorToKinesisStreamInOrderTheyWereAppended() {
        // Given
        KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration = createSimulatedEnvironmentConfigurationFor(kinesisClient);
        KinesisAuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(simulatedEnvironmentConfiguration, kinesisClient.getStreamName(), true, 10000, 10, SECONDS);

        String authorId = UUID.randomUUID().toString();

        AuthorUpdate firstUpdate = anAuthorUpdate().withId(authorId).withAuthor(anAuthor().withId(authorId).withName("First").withRemoval(false)).build();
        AuthorUpdate secondUpdate = anAuthorUpdate().withId(authorId).withAuthor(anAuthor().withId(authorId).withName("Second").withRemoval(false)).build();
        AuthorUpdate thirdUpdate = anAuthorUpdate().withId(authorId).withAuthor(anAuthor().withId(authorId).withName("Third").withRemoval(true)).build();

        // When
        kinesisAuthorUpdatesStream.publish(firstUpdate);
        kinesisAuthorUpdatesStream.publish(secondUpdate);
        kinesisAuthorUpdatesStream.publish(thirdUpdate);

        // Then
        assertThat(() -> bodiesOf(kinesisClient.records()), eventuallySatisfies(containsInRelativeOrder(
                allOf(hasJsonPath("$.id", is(equalTo(authorId))), hasJsonPath("$.author.id", is(equalTo(authorId))), hasJsonPath("$.author.name", is(equalTo("First"))), hasJsonPath("$.author.removed", is(equalTo(false)))),
                allOf(hasJsonPath("$.id", is(equalTo(authorId))), hasJsonPath("$.author.id", is(equalTo(authorId))), hasJsonPath("$.author.name", is(equalTo("Second"))), hasJsonPath("$.author.removed", is(equalTo(false)))),
                allOf(hasJsonPath("$.id", is(equalTo(authorId))), hasJsonPath("$.author.id", is(equalTo(authorId))), hasJsonPath("$.author.name", is(equalTo("Third"))), hasJsonPath("$.author.removed", is(equalTo(true))))
        )).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    private static List<String> bodiesOf(List<Record> records) {
        return records.stream().map(KinesisUtils::getKinesisRecordContent).collect(Collectors.toList());
    }

    private static KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration createSimulatedEnvironmentConfigurationFor(KinesisClient kinesisClient) {
        return new KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration(
                kinesisClient.getAwsRegion(),
                kinesisClient.getKinesisEndpoint(),
                kinesisClient.getAwsAccessKey(),
                kinesisClient.getAwsSecretKey()
        );
    }
}
