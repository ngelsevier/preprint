package com.ssrn.search.papers_consumer;

import com.ssrn.search.domain.Paper;
import com.ssrn.search.papers_consumer.fake_papers_service.FakePaperService;
import com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper;
import com.ssrn.test.support.kinesis.KinesisStream;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper.SubmissionStage.SUBMITTED;
import static com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaperBuilder.aPaper;
import static com.ssrn.search.papers_consumer.matchers.PaperMarkedIrrelevantMatcher.markedIrrelevant;
import static com.ssrn.search.papers_consumer.matchers.PaperMarkedPrivateMatcher.markedPrivate;
import static com.ssrn.search.papers_consumer.matchers.PaperMarkedRestrictedMatcher.markedRestricted;
import static com.ssrn.search.papers_consumer.matchers.PaperMatcher.aPaperWith;
import static com.ssrn.search.papers_consumer.matchers.PaperWithIdMatcher.id;
import static com.ssrn.search.papers_consumer.matchers.PaperWithKeywordsMatcher.keywords;
import static com.ssrn.search.papers_consumer.matchers.PaperWithSubmissionStageMatcher.submissionStage;
import static com.ssrn.search.papers_consumer.matchers.PaperWithTitleMatcher.title;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.onABackgroundThread;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.junit.Assert.assertThat;

public class KinesisPapersStreamTest {
    private static final boolean LOG_INDIVIDUAL_PAPERS = Boolean.TRUE;

    private KinesisPapersStream.SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration;
    private FakePaperService fakePaperService;
    private KinesisStream kinesisStream;

    @Before
    public void setup() {
        fakePaperService = new FakePaperService();
        kinesisStream = new KinesisStream("localhost", 4567, "papers-integration-tests", 2);

        simulatedEnvironmentConfiguration = new KinesisPapersStream.SimulatedEnvironmentConfiguration(
                kinesisStream.getAwsRegion(),
                "http://localhost:8000",
                kinesisStream.getKinesisEndpoint(),
                kinesisStream.getAwsAccessKey(),
                kinesisStream.getAwsSecretKey()
        );
    }

    @Test
    public void shouldNotifyThatBatchOfPaperUpdatesHasBeenReceivedInOrderTheyWereWrittenToKinesisStreamShard() throws Exception {
        // Given
        try (PapersStream kinesisPapersStream = new KinesisPapersStream(uniqueKclApplicationName(), kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)) {
            List<Paper> receivedPapers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisPapersStream.onPapersReceived(receivedPapers::addAll));

            // When
            PaperServicePaper paperUpdate = aPaper()
                    .withTitle("Initial paper title")
                    .withAuthorIds("234", "3", "45")
                    .withKeywords("any string")
                    .build();

            PaperServicePaper anotherUpdateForSamePaper = aPaper()
                    .withId(paperUpdate.getId())
                    .withTitle("Updated paper title")
                    .build();

            PaperServicePaper markPaperAsPrivateUpdateForSamePaper = aPaper()
                    .withId(paperUpdate.getId())
                    .withTitle("Paper now private")
                    .thatIsPrivate()
                    .build();

            PaperServicePaper submittedPaper = aPaper()
                    .withId(paperUpdate.getId())
                    .withTitle("Paper now submitted")
                    .withSubmissionStage(SUBMITTED)
                    .build();

            PaperServicePaper markPaperAsIrrelevantUpdateForSamePaper = aPaper()
                    .withId(paperUpdate.getId())
                    .withTitle("Paper now irrelevant")
                    .thatIsIrrelevant()
                    .build();

            PaperServicePaper markPaperAsRestrictedUpdateForSamePaper = aPaper()
                    .withId(paperUpdate.getId())
                    .withTitle("Paper is restricted")
                    .thatIsRestricted()
                    .build();

            fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), paperUpdate, anotherUpdateForSamePaper, markPaperAsPrivateUpdateForSamePaper, submittedPaper, markPaperAsIrrelevantUpdateForSamePaper, markPaperAsRestrictedUpdateForSamePaper);

            // Then
            assertThat(() -> receivedPapers, eventuallySatisfies(
                    containsInRelativeOrder(
                            aPaperWith(title(paperUpdate.getTitle()), keywords("any string")),
                            aPaperWith(title(anotherUpdateForSamePaper.getTitle())),
                            aPaperWith(title(markPaperAsPrivateUpdateForSamePaper.getTitle()), markedPrivate()),
                            aPaperWith(title(submittedPaper.getTitle()), submissionStage("SUBMITTED")),
                            aPaperWith(title(markPaperAsIrrelevantUpdateForSamePaper.getTitle()), markedIrrelevant()),
                            aPaperWith(title(markPaperAsRestrictedUpdateForSamePaper.getTitle()), markedRestricted())
                    )
            ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            Paper streamedPaperWithFirstPaperId = receivedPapers.stream()
                    .filter(paper -> paper.getId().equals(paperUpdate.getId()))
                    .findFirst()
                    .get();

            assertThat(streamedPaperWithFirstPaperId.getTitle(), is(equalTo(paperUpdate.getTitle())));
            assertThat(streamedPaperWithFirstPaperId.getAuthorIds(), is(equalTo(paperUpdate.getAuthorIds())));
        }
    }

    @Test
    public void shouldOnlyNotifyAboutPaperUpdatesAfterPreviousCheckpoint() throws Exception {
        // Given

        String applicationName = uniqueKclApplicationName();
        PaperServicePaper firstPaperServicePaper = aPaper().build();

        try (PapersStream kinesisPapersStream = new KinesisPapersStream(
                applicationName, kinesisStream.getStreamName(),
                simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)
        ) {
            List<Paper> receivedPapers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisPapersStream.onPapersReceived(receivedPapers::addAll));

            fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), firstPaperServicePaper);

            assertThat(() -> receivedPapers, eventuallySatisfies(hasItem(aPaperWith(id(firstPaperServicePaper.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        }

        // When

        PaperServicePaper secondPaper = aPaper().build();
        PaperServicePaper thirdPaper = aPaper().build();
        fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), secondPaper, thirdPaper);

        try (PapersStream kinesisPapersStream2 = new KinesisPapersStream(
                applicationName, kinesisStream.getStreamName(),
                simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)
        ) {

            List<Paper> receivedEvents2 = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisPapersStream2.onPapersReceived(receivedEvents2::addAll));
            // Then

            assertThat(() -> receivedEvents2, eventuallySatisfies(
                    allOf(
                            hasItems(aPaperWith(id(secondPaper.getId())), aPaperWith(id(thirdPaper.getId()))),
                            not(hasItem(aPaperWith(id(firstPaperServicePaper.getId()))))
                    )
            ).within(60, SECONDS, checkingEvery(100, MILLISECONDS)));

        }
    }

    @Test
    public void shouldFinishNotifyingAboutBatchOfPaperUpdatesBeforeShuttingDown() throws Exception {
        // Given
        PaperServicePaper firstPaperServicePaper = aPaper().build();
        PaperServicePaper secondPaperServicePaper = aPaper().build();
        fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), firstPaperServicePaper, secondPaperServicePaper);

        String applicationName = uniqueKclApplicationName();

        CountDownLatch secondPaperProcessed = new CountDownLatch(1);
        CountDownLatch kinesisPaperEventStreamCloseRequested = new CountDownLatch(1);
        CountDownLatch kinesisPaperEventStreamClosed = new CountDownLatch(1);

        PapersStream kinesisPapersStream = new KinesisPapersStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS);

        onABackgroundThread(() -> {
            try {
                secondPaperProcessed.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            onABackgroundThread(() -> {
                sleepFor(1, SECONDS);
                kinesisPaperEventStreamCloseRequested.countDown();
            });

            try {
                // When
                kinesisPapersStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            kinesisPaperEventStreamClosed.countDown();
        });

        List<Paper> receivedPapers = new CopyOnWriteArrayList<>();

        onABackgroundThread(() -> kinesisPapersStream.onPapersReceived(papers -> {
            papers.forEach(paper -> {
                receivedPapers.add(paper);

                if (secondPaperServicePaper.getId().equals(paper.getId())) {
                    secondPaperProcessed.countDown();

                    try {
                        kinesisPaperEventStreamCloseRequested.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }));

        kinesisPaperEventStreamClosed.await();

        assertThat(receivedPapers, hasItems(aPaperWith(id(firstPaperServicePaper.getId())), aPaperWith(id(secondPaperServicePaper.getId()))));

        PaperServicePaper thirdPaperServicePaper = aPaper().build();
        fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), thirdPaperServicePaper);

        try (PapersStream secondKinesisPapersStream = new KinesisPapersStream(applicationName, kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)) {
            List<Paper> eventsReceivedBySecondsKinesisPaperEventsStream = new CopyOnWriteArrayList<>();

            onABackgroundThread(() -> secondKinesisPapersStream.onPapersReceived(eventsReceivedBySecondsKinesisPaperEventsStream::addAll));

            // Then
            assertThat(() -> eventsReceivedBySecondsKinesisPaperEventsStream, eventuallySatisfies(
                    allOf(
                            hasItem(aPaperWith(id(thirdPaperServicePaper.getId()))),
                            not(hasItems(aPaperWith(id(firstPaperServicePaper.getId())), aPaperWith(id(secondPaperServicePaper.getId())))))
            ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
        }

    }

    @Test
    public void shouldNotifyAboutPaperUpdatesAlreadyInKinesisStream() throws Exception {
        // Given
        try (PapersStream kinesisPapersStream = new KinesisPapersStream(uniqueKclApplicationName(), kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)) {
            PaperServicePaper paper = aPaper().build();
            fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), paper);

            // When
            List<Paper> receivedPapers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisPapersStream.onPapersReceived(receivedPapers::addAll));

            // Then
            assertThat(() -> receivedPapers, eventuallySatisfies(hasItem(aPaperWith(id(paper.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSkipAnyUnparseablePaperUpdatesInBatch() throws Exception {
        // Given
        try (PapersStream kinesisPapersStream = new KinesisPapersStream(uniqueKclApplicationName(), kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)) {
            List<Paper> receivedPapers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisPapersStream.onPapersReceived(receivedPapers::addAll));

            // When
            fakePaperService.hasEmittedUnprocessablePaperIntoKinesisStream(kinesisStream.getStreamName());

            PaperServicePaper paper = aPaper().build();
            fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), paper);

            // Then
            assertThat(() -> receivedPapers, eventuallySatisfies(hasItem(aPaperWith(id(paper.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldAdvanceToReadingNewShardsAfterAReshard() throws Exception {
        // Given
        try (PapersStream kinesisPapersStream = new KinesisPapersStream(uniqueKclApplicationName(), kinesisStream.getStreamName(), simulatedEnvironmentConfiguration, LOG_INDIVIDUAL_PAPERS)
        ) {
            List<Paper> receivedPapers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> kinesisPapersStream.onPapersReceived(receivedPapers::addAll));

            PaperServicePaper firstPaperServicePaper = aPaper().withTitle("Paper streamed before reshard").build();
            fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), firstPaperServicePaper);
            assertThat(() -> receivedPapers, eventuallySatisfies(hasItem(aPaperWith(id(firstPaperServicePaper.getId()))))
                    .within(60, SECONDS, checkingEvery(100, MILLISECONDS)));

            assertThat(kinesisStream.getShardCount(), is(equalTo(2)));
            kinesisStream.evenlySplitNthOpenShard(0);
            assertThat(kinesisStream.getShardCount(), is(equalTo(4)));

            PaperServicePaper secondPaperServicePaper = aPaper().withTitle("Paper streamed after reshard").build();

            // When
            fakePaperService.hasEmittedPapersIntoKinesisStream(kinesisStream.getStreamName(), secondPaperServicePaper);

            // Then
            assertThat(() -> receivedPapers, eventuallySatisfies(hasItem(aPaperWith(id(secondPaperServicePaper.getId()))))
                    .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    private static String uniqueKclApplicationName() {
        return String.format("Test-%s", UUID.randomUUID());
    }

}
