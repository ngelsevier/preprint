package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.author_updates_subscriber.fake_authors_service.AuthorServiceAuthorUpdate;
import com.ssrn.search.author_updates_subscriber.fake_authors_service.FakeAuthorService;
import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.test.support.kinesis.KinesisStream;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static com.ssrn.search.author_updates_subscriber.fake_authors_service.AuthorServiceAuthorUpdateBuilder.anAuthorUpdate;
import static com.ssrn.search.author_updates_subscriber.matchers.AuthorUpdateMatcher.anAuthorUpdateWith;
import static com.ssrn.search.author_updates_subscriber.matchers.AuthorUpdateWithIdMatcher.id;
import static com.ssrn.search.author_updates_subscriber.matchers.AuthorUpdateWithNameMatcher.name;
import static com.ssrn.search.author_updates_subscriber.matchers.AuthorUpdateWithRemovalMatcher.removed;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.onABackgroundThread;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.junit.Assert.assertThat;

public class KinesisAuthorUpdatesStreamTest {
    private static final boolean LOG_INDIVIDUAL_AUTHOR_UPDATES = Boolean.TRUE;

    private KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration;
    private FakeAuthorService fakeAuthorService;
    private KinesisStream kinesisStream;

    @Before
    public void setup() {
        fakeAuthorService = new FakeAuthorService();
        kinesisStream = new KinesisStream("localhost", 4567, "author-updates-integration-tests", 1);

        simulatedEnvironmentConfiguration = new KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration(
                kinesisStream.getAwsRegion(),
                "http://localhost:8000",
                kinesisStream.getKinesisEndpoint(),
                kinesisStream.getAwsAccessKey(),
                kinesisStream.getAwsSecretKey()
        );
    }

    @Test
    public void shouldNotifyThatBatchOfAuthorUpdatesHasBeenReceivedInOrderTheyWereWrittenToKinesisStreamShard() throws Exception {
        // Given
        try (AuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(uniqueKclApplicationName(),kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES))
        {
            List<AuthorUpdate> receivedAuthors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisAuthorUpdatesStream.onAuthorUpdatesReceived(receivedAuthors::addAll));

            // Given

            AuthorServiceAuthorUpdate authorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).withRemoved(false).build();
            AuthorServiceAuthorUpdate anotherUpdateForSameAuthor = anAuthorUpdate().withId(authorUpdate.getId()).withName(UUID.randomUUID().toString()).withRemoved(false).build();
            AuthorServiceAuthorUpdate authorToBeRemoved = anAuthorUpdate().withName(UUID.randomUUID().toString()).withRemoved(true).build();

            // When

            fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), authorUpdate, anotherUpdateForSameAuthor, authorToBeRemoved);

            // Then

            assertThat(() -> receivedAuthors, eventuallySatisfies(
                    containsInRelativeOrder(
                            anAuthorUpdateWith(id(authorUpdate.getId()), name(authorUpdate.getAuthor().getName()), removed(authorUpdate.getAuthor().isRemoved())),
                            anAuthorUpdateWith(id(anotherUpdateForSameAuthor.getId()), name(anotherUpdateForSameAuthor.getAuthor().getName()), removed(anotherUpdateForSameAuthor.getAuthor().isRemoved())),
                            anAuthorUpdateWith(id(authorToBeRemoved.getId()), name(authorToBeRemoved.getAuthor().getName()), removed(authorToBeRemoved.getAuthor().isRemoved()))
                    )
            ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));

        }
    }

    @Test
    public void shouldOnlyNotifyAboutAuthorUpdatesAfterPreviousCheckpoint() throws Exception {
        // Given

        String applicationName = uniqueKclApplicationName();
        AuthorServiceAuthorUpdate firstAuthorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();

        try (AuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES)) {
            List<AuthorUpdate> receivedAuthorUpdates = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisAuthorUpdatesStream.onAuthorUpdatesReceived(receivedAuthorUpdates::addAll));

            fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), firstAuthorUpdate);

            assertThat(() -> receivedAuthorUpdates, eventuallySatisfies(hasItem(anAuthorUpdateWith(
                    name(firstAuthorUpdate.getAuthor().getName()),
                    id(firstAuthorUpdate.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        }

        // When

        AuthorServiceAuthorUpdate secondAuthorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();
        AuthorServiceAuthorUpdate thirdAuthorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();

        fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), secondAuthorUpdate, thirdAuthorUpdate);

        try (AuthorUpdatesStream kinesisAuthorUpdatesStream2 = new KinesisAuthorUpdatesStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES)) {

            List<AuthorUpdate> receivedAuthorUpdates2 = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisAuthorUpdatesStream2.onAuthorUpdatesReceived(receivedAuthorUpdates2::addAll));
            // Then

            assertThat(() -> receivedAuthorUpdates2, eventuallySatisfies(
                    allOf(
                            hasItems(anAuthorUpdateWith(id(secondAuthorUpdate.getId())), anAuthorUpdateWith(id(thirdAuthorUpdate.getId()))),
                            not(hasItem(anAuthorUpdateWith(id(firstAuthorUpdate.getId()))))
                    )
            ).within(60, SECONDS, checkingEvery(100, MILLISECONDS)));

        }
    }

    @Test
    public void shouldFinishNotifyingAboutBatchOfAuthorUpdatesBeforeShuttingDown() throws Exception {
        // Given
        AuthorServiceAuthorUpdate firstAuthorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();
        AuthorServiceAuthorUpdate secondAuthorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();
        fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), firstAuthorUpdate, secondAuthorUpdate);

        String applicationName = uniqueKclApplicationName();

        CountDownLatch secondAuthorUpdateProcessed = new CountDownLatch(1);
        CountDownLatch kinesisAuthorUpdatesStreamCloseRequested = new CountDownLatch(1);
        CountDownLatch kinesisAuthorUpdatesStreamClosed = new CountDownLatch(1);

        AuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES);

        onABackgroundThread(() -> {
            try {
                secondAuthorUpdateProcessed.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            onABackgroundThread(() -> {
                sleepFor(1, SECONDS);
                kinesisAuthorUpdatesStreamCloseRequested.countDown();
            });

            try {
                // When
                kinesisAuthorUpdatesStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            kinesisAuthorUpdatesStreamClosed.countDown();
        });

        List<AuthorUpdate> receivedAuthorUpdates = new CopyOnWriteArrayList<>();

        onABackgroundThread(() -> kinesisAuthorUpdatesStream.onAuthorUpdatesReceived(authorUpdates -> {
            authorUpdates.forEach(update -> {
                receivedAuthorUpdates.add(update);

                if (secondAuthorUpdate.getId().equals(update.getId())) {
                    secondAuthorUpdateProcessed.countDown();

                    try {
                        kinesisAuthorUpdatesStreamCloseRequested.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }));

        kinesisAuthorUpdatesStreamClosed.await();

        assertThat(receivedAuthorUpdates, hasItems(anAuthorUpdateWith(id(firstAuthorUpdate.getId())), anAuthorUpdateWith(id(secondAuthorUpdate.getId()))));

        AuthorServiceAuthorUpdate thirdAuthorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();
        fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), thirdAuthorUpdate);

        try (AuthorUpdatesStream secondKinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES)) {
            List<AuthorUpdate> authorUpdatesReceivedBySecondKinesisAuthorUpdatesStream = new CopyOnWriteArrayList<>();

            onABackgroundThread(() -> secondKinesisAuthorUpdatesStream.onAuthorUpdatesReceived(authorUpdatesReceivedBySecondKinesisAuthorUpdatesStream::addAll));

            // Then
            assertThat(() -> authorUpdatesReceivedBySecondKinesisAuthorUpdatesStream, eventuallySatisfies(
                    allOf(
                            hasItem(anAuthorUpdateWith(id(thirdAuthorUpdate.getId()))),
                            not(hasItems(anAuthorUpdateWith(id(firstAuthorUpdate.getId())), anAuthorUpdateWith(id(secondAuthorUpdate.getId())))))
            ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
        }

    }

    @Test
    public void shouldNotifyAboutAuthorUpdatesAlreadyInKinesisStream() throws Exception {
        // Given
        String applicationName = uniqueKclApplicationName();
        try (AuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES)) {
            AuthorServiceAuthorUpdate authorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();
            fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), authorUpdate);

            // When
            List<AuthorUpdate> receivedAuthorUpdates = new CopyOnWriteArrayList<>();
            onABackgroundThread(() ->kinesisAuthorUpdatesStream.onAuthorUpdatesReceived(receivedAuthorUpdates::addAll));

            // Then
            assertThat(() -> receivedAuthorUpdates, eventuallySatisfies(hasItem(anAuthorUpdateWith(id(authorUpdate.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSkipAnyUnparseableAuthorUpdatesInBatch() throws Exception {
        // Given
        String applicationName = uniqueKclApplicationName();
        try (AuthorUpdatesStream kinesisAuthorUpdatesStream = new KinesisAuthorUpdatesStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_AUTHOR_UPDATES)) {
            List<AuthorUpdate> receivedAuthorUpdates = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisAuthorUpdatesStream.onAuthorUpdatesReceived(receivedAuthorUpdates::addAll));

            // When
            fakeAuthorService.hasEmittedUnprocessableAuthorUpdateIntoKinesisStream(kinesisStream.getStreamName());

            AuthorServiceAuthorUpdate authorUpdate = anAuthorUpdate().withName(UUID.randomUUID().toString()).build();
            fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisStream.getStreamName(), authorUpdate);

            // Then
            assertThat(() -> receivedAuthorUpdates, eventuallySatisfies(hasItem(anAuthorUpdateWith(id(authorUpdate.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    private static String uniqueKclApplicationName() {
        return String.format("Test-%s", UUID.randomUUID());
    }

}
