package com.ssrn.papers.postgres;

import com.ssrn.papers.domain.*;
import com.ssrn.papers.postgres.support.PartitionedMd5Range;
import com.ssrn.papers.shared.test_support.event.matchers.PaperWithSubmissionStageMatcher;
import com.ssrn.papers.shared.test_support.postgres.PostgresPapersDatabaseIntegrationTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ssrn.papers.domain.SubmissionStage.*;
import static com.ssrn.papers.postgres.support.PartitionedMd5Range.partitionedMd5Range;
import static com.ssrn.papers.shared.test_support.event.EventBuilder.aTitleChangedEvent;
import static com.ssrn.papers.shared.test_support.event.PaperBuilder.aPaper;
import static com.ssrn.papers.shared.test_support.event.matchers.APaperMatcher.aPaperWith;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperMarkedIrrelevantMatcher.markedIrrelevant;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperMarkedPrivateMatcher.markedPrivate;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperMarkedRestrictedMatcher.markedRestricted;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperWithAuthorIdsMatcher.*;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperWithIdMatcher.id;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperWithKeywordsMatcher.keywords;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperWithSubmissionStageMatcher.submissionStage;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperWithTitleMatcher.title;
import static com.ssrn.papers.shared.test_support.event.matchers.PaperWithVersionMatcher.version;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.RepeaterFluentSyntax.repeat;
import static com.ssrn.test.support.utils.TaskWhilstAnotherTaskRepeats.whilstRepeating;
import static com.ssrn.test.support.utils.ThreadingUtils.onABackgroundThread;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

public class PostgresPaperRepositoryTest extends PostgresPapersDatabaseIntegrationTest {

    private DBI dbi;
    private PostgresDatabaseClientConfiguration postgresDatabaseClientConfiguration;
    private PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration;

    @Before
    public void before() {
        postgresDatabase().ensureStarted();

        dbi = new DBI(
                String.format("jdbc:postgresql://%s:%d/%s", postgresDatabase().getHost(), postgresDatabase().getPort(), postgresDatabase().getDatabaseName()),
                postgresDatabase().getReplicatorUsername(),
                postgresDatabase().getReplicatorPassword()
        );

        postgresDatabaseClientConfiguration = new PostgresDatabaseClientConfiguration(
                postgresDatabase().getHost(),
                postgresDatabase().getPort(),
                postgresDatabase().getDatabaseName(),
                postgresDatabase().getEventsEmitterUsername(),
                postgresDatabase().getEventsEmitterPassword(),
                10, SECONDS,
                30, SECONDS
        );

        postgresReplicationStreamingConfiguration = new PostgresReplicationStreamingConfiguration(
                getTestSlotNamePrefix(),
                30, SECONDS,
                20, SECONDS,
                10, SECONDS
        );
    }

    @Test
    public void shouldNotifyThatAPaperIsUpdatedWhenANewPaperIsSavedToTheDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper privatePaper = aPaper()
                    .withTitle("Paper's title")
                    .withAuthorIds("5", "3", "6")
                    .withPaperPrivate(true)
                    .withPaperIrrelevant(false)
                    .build();

            Paper publicPaper = aPaper()
                    .withPaperPrivate(false)
                    .withPaperIrrelevant(false)
                    .build();

            Paper irrelevantPaper = aPaper()
                    .withPaperPrivate(false)
                    .withPaperIrrelevant(true)
                    .build();

            Paper aRejectedPaper = aPaper()
                    .withPaperPrivate(false)
                    .withPaperIrrelevant(false)
                    .withSubmissionStage(REJECTED)
                    .build();

            Paper aRestrictedPaper = aPaper()
                    .withPaperPrivate(false)
                    .withPaperIrrelevant(false)
                    .withPaperRestricted(true)
                    .build();

            Paper aPaperWithKeywords = aPaper()
                    .withPaperPrivate(false)
                    .withPaperIrrelevant(false)
                    .withPaperRestricted(false)
                    .withKeywords("any string")
                    .build();

            // When
            replicatorPaperRepository.save(privatePaper);
            replicatorPaperRepository.save(publicPaper);
            replicatorPaperRepository.save(irrelevantPaper);
            replicatorPaperRepository.save(aRejectedPaper);
            replicatorPaperRepository.save(aRestrictedPaper);
            replicatorPaperRepository.save(aPaperWithKeywords);


            // Then
            assertThat(() -> papers, eventuallySatisfies(allOf(
                    hasItem(aPaperWith(
                            id(String.valueOf(privatePaper.getId())),
                            title("Paper's title"),
                            authorIds("5", "3", "6"),
                            markedPrivate(true),
                            markedIrrelevant(false)
                    )),
                    hasItem(aPaperWith(
                            id(String.valueOf(publicPaper.getId())),
                            markedPrivate(false),
                            markedIrrelevant(false)
                    )),
                    hasItem(aPaperWith(
                            id(String.valueOf(irrelevantPaper.getId())),
                            markedPrivate(false),
                            markedIrrelevant(true)
                    )),
                    hasItem(aPaperWith(
                            id(String.valueOf(aRejectedPaper.getId())),
                            markedPrivate(false),
                            markedIrrelevant(false),
                            submissionStage(REJECTED)
                    )),
                    hasItem(aPaperWith(
                            id(String.valueOf(aRestrictedPaper.getId())),
                            markedPrivate(false),
                            markedIrrelevant(false),
                            markedRestricted(true),
                            keywords(null)
                    )),
                    hasItem(aPaperWith(
                            id(String.valueOf(aPaperWithKeywords.getId())),
                            markedPrivate(false),
                            markedIrrelevant(false),
                            markedRestricted(false),
                            keywords("any string")
                    ))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test(expected = RetryingSupplier.TimeoutException.class)
    public void shouldPropagateExceptionsThrownOnWorkerThreadsToCallingThread() {
        // Given
        PostgresDatabaseClientConfiguration impatientConfiguration = new PostgresDatabaseClientConfiguration(
                "non-existent-host",
                postgresDatabaseClientConfiguration.getPort(),
                postgresDatabaseClientConfiguration.getDatabaseName(),
                postgresDatabaseClientConfiguration.getUsername(),
                postgresDatabaseClientConfiguration.getPassword(),
                postgresDatabaseClientConfiguration.getConnectionTimeout(),
                postgresDatabaseClientConfiguration.getConnectionTimeoutUnit(),
                0,
                postgresDatabaseClientConfiguration.getConnectionAttemptsTimeoutUnit()
        );

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(impatientConfiguration, postgresReplicationStreamingConfiguration, dbi, 4, 1)) {
            emitterPostgresPaperRepository.onPaperUpdated(ignored -> {
            });
        }
    }

    @Test
    public void shouldRecoverFromLossOfReplicationConnection() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        PostgresDatabaseClientConfiguration impatientDatabaseClientConfiguration = new PostgresDatabaseClientConfiguration(
                postgresDatabaseClientConfiguration.getHost(),
                postgresDatabaseClientConfiguration.getPort(),
                postgresDatabaseClientConfiguration.getDatabaseName(),
                postgresDatabaseClientConfiguration.getUsername(),
                postgresDatabaseClientConfiguration.getPassword(),
                2, SECONDS,
                postgresDatabaseClientConfiguration.getConnectionAttemptsTimeout(), postgresDatabaseClientConfiguration.getConnectionAttemptsTimeoutUnit()
        );

        PostgresReplicationStreamingConfiguration impatientReplicationStreamingConfiguration = new PostgresReplicationStreamingConfiguration(
                this.postgresReplicationStreamingConfiguration.getSlotNamePrefix(),
                this.postgresReplicationStreamingConfiguration.getReplicationStreamCreationAttemptsTimeout(), this.postgresReplicationStreamingConfiguration.getReplicationStreamCreationAttemptsTimeoutUnit(),
                this.postgresReplicationStreamingConfiguration.getReplicationSlotDropAttemptsTimeout(), this.postgresReplicationStreamingConfiguration.getReplicationSlotDropAttemptsTimeoutUnits(),
                2, SECONDS
        );


        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(impatientDatabaseClientConfiguration, impatientReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);


            Paper paper = aPaper().withTitle("Paper's title").build();

            // When
            postgresDatabase().ensureStopped();
            sleepFor(6, SECONDS);
            postgresDatabase().ensureStarted();
            replicatorPaperRepository.save(paper);
            assertThat(papers, not(hasItem(aPaperWith(id(String.valueOf(paper.getId())), title("Paper's title")))));

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItem(aPaperWith(id(String.valueOf(paper.getId())), title("Paper's title"))))
                    .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldRaiseEachPaperUpdateNotificationsOnOneOfASpecifiedNumberOfThreads() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        int maxConcurrentPaperUpdates = 4;

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, maxConcurrentPaperUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();
            List<String> notifiedPaperIds = new CopyOnWriteArrayList<>();

            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(p -> {
                uniqueNotificationThreadIds.add(Thread.currentThread().getId());
                notifiedPaperIds.add(p.getId());
            }));

            PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(maxConcurrentPaperUpdates);

            String[] partitionedPaperIds = IntStream.range(0, maxConcurrentPaperUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new);

            whilstRepeating(() -> Arrays.stream(partitionedPaperIds).forEach(paperId -> replicatorPaperRepository.save(aPaper().withId(paperId).build())))
                    .waitUntil(() -> uniqueNotificationThreadIds.size() == maxConcurrentPaperUpdates)
                    .forNoMoreThan(30, SECONDS);

            String[] morePartitionedPaperIds = IntStream.range(0, maxConcurrentPaperUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new);

            // When
            Arrays.stream(morePartitionedPaperIds)
                    .forEach(paperId -> replicatorPaperRepository.save(aPaper().withId(paperId).build()));

            // Then
            assertThat(() -> notifiedPaperIds, eventuallySatisfies(hasItems(morePartitionedPaperIds))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            Arrays.stream(morePartitionedPaperIds)
                    .forEach(paperId -> assertThat(notifiedPaperIds.stream().filter(id -> id.equals(paperId)).count(), is(equalTo(1L))));

            assertThat(uniqueNotificationThreadIds, hasSize(maxConcurrentPaperUpdates));
        }
    }

    @Test
    public void shouldDropAnySuperfluousSlotsBeforeListeningForPaperUpdateNotifications() {
        // Given

        PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);

        int initialMaxConcurrentPaperUpdates = 4;

        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, initialMaxConcurrentPaperUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();

            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(p -> {
                uniqueNotificationThreadIds.add(Thread.currentThread().getId());
            }));

            PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(initialMaxConcurrentPaperUpdates);

            String[] partitionedPaperIds = IntStream.range(0, initialMaxConcurrentPaperUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new);

            whilstRepeating(() -> Arrays.stream(partitionedPaperIds).forEach(paperId -> replicatorPostgresPaperRepository.save(aPaper().withId(paperId).build())))
                    .waitUntil(() -> uniqueNotificationThreadIds.size() == initialMaxConcurrentPaperUpdates)
                    .forNoMoreThan(30, SECONDS);

            assertThat(postgresDatabase().replicationSlotCount(), is(equalTo(4)));
        }

        // When
        int reducedMaxConcurrentPaperUpdates = 2;

        try (PostgresPaperRepository anotherEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, reducedMaxConcurrentPaperUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();

            onABackgroundThread(() -> anotherEventsEmitterPostgresPaperRepository.onPaperUpdated(p -> uniqueNotificationThreadIds.add(Thread.currentThread().getId())));

            PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(reducedMaxConcurrentPaperUpdates);

            String[] partitionedPaperIds = IntStream.range(0, reducedMaxConcurrentPaperUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new);

            whilstRepeating(() -> Arrays.stream(partitionedPaperIds).forEach(paperId -> replicatorPostgresPaperRepository.save(aPaper().withId(paperId).build())))
                    .waitUntil(() -> uniqueNotificationThreadIds.size() == reducedMaxConcurrentPaperUpdates)
                    .forNoMoreThan(30, SECONDS);

            assertThat(postgresDatabase().replicationSlotCount(), is(equalTo(2)));
        }
    }

    @Test
    public void shouldRaiseAllPaperUpdateNotificationsForAGivenPaperOnTheSameThread() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        int maxConcurrentPaperUpdates = 4;

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, maxConcurrentPaperUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();
            List<String> notifiedPaperIds = new CopyOnWriteArrayList<>();

            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(p -> {
                uniqueNotificationThreadIds.add(Thread.currentThread().getId());
                notifiedPaperIds.add(p.getId());
            }));

            whilstRepeating(() -> replicatorPaperRepository.save(aPaper().withId("10").build()))
                    .waitUntil(() -> notifiedPaperIds.size() > 0)
                    .forNoMoreThan(30, SECONDS);

            // When
            repeat(() -> replicatorPaperRepository.save(aPaper().withId("10").build()))
                    .times(50);

            // Then
            assertThat(() -> notifiedPaperIds, eventuallySatisfies(hasSize(greaterThanOrEqualTo(50)))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            assertThat(uniqueNotificationThreadIds, hasSize(1));
        }
    }

    @Test
    public void shouldNotifyOfPaperUpdatesInOrderTheyOccur() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            // When
            replicatorPaperRepository.save(aPaper().withId("1").build());
            replicatorPaperRepository.save(aPaper().withId("2").build());
            replicatorPaperRepository.save(aPaper().withId("3").build());

            // Then
            assertThat(() -> papers, eventuallySatisfies(containsInRelativeOrder(
                    aPaperWith(id("1")),
                    aPaperWith(id("2")),
                    aPaperWith(id("3"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyThatAPaperIsUpdatedWhenAnExistingPaperIsSavedToTheDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper = aPaper().withTitle("Initial title").build();
            replicatorPaperRepository.save(paper);

            Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent()
                    .withTitle("Updated title")
                    .withStandardEventProperties(x -> x.withEntityVersion(paper.getVersion() + 1))
                    .build();

            paper.apply(titleChangedEvent);

            // When
            replicatorPaperRepository.save(paper);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItem(aPaperWith(id(String.valueOf(paper.getId())), title("Updated title"))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyOfRepeatedSavesOfTheSamePaper() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper = aPaper().withVersion(2).build();

            replicatorPaperRepository.save(paper);

            // When
            replicatorPaperRepository.save(paper);

            // Then
            assertThat(() -> papers, eventuallySatisfies(containsInRelativeOrder(
                    aPaperWith(id(String.valueOf(paper.getId()))),
                    aPaperWith(id(String.valueOf(paper.getId())))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingSingleQuotes() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            String title = String.format("Paper's title with single quote %s", UUID.randomUUID());
            Paper paper = aPaper().withTitle(title).build();

            // When
            replicatorPaperRepository.save(paper);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItem(aPaperWith(id(String.valueOf(paper.getId())), title(title))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingUtf8Characters() {
        // Given
        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));

            PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            Paper paperWithUtf8CharactersInTitle = aPaper().withTitle("The Title contains some UTF-8 characters like Фінансово").build();

            // When
            replicatorPostgresPaperRepository.save(paperWithUtf8CharactersInTitle);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(title("The Title contains some UTF-8 characters like Фінансово"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingUnicodeLineSeparatorsCharacters() {
        // Given
        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));

            PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            Paper paperWithUnicodeLineSeparatorInTitle = aPaper().withTitle("The Title contains a\u2028line separator").build();

            // When
            replicatorPostgresPaperRepository.save(paperWithUnicodeLineSeparatorInTitle);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(title("The Title contains a\u2028line separator"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingLineBreaks() {
        // Given
        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));

            PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            Paper paperWithLineBreakInTitle = aPaper().withTitle("The Title contains a\nline break").build();

            // When
            replicatorPostgresPaperRepository.save(paperWithLineBreakInTitle);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(title("The Title contains a\nline break"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldFilterOutNullTerminatorCharactersFromEntityJson() {
        // Given
        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));

            PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            Paper paperWithNullTerminatorCharacterInTitle = aPaper().withTitle("a title containing null\u0000 terminator characters").build();

            // When
            replicatorPostgresPaperRepository.save(paperWithNullTerminatorCharacterInTitle);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(title("a title containing null terminator characters"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldUpdateExistingPaperInDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        Paper paper = aPaper().withTitle("Initial title").build();
        replicatorPaperRepository.save(paper);

        Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent()
                .withTitle("Updated title")
                .withStandardEventProperties(x -> x.withEntityVersion(paper.getVersion() + 1))
                .build();

        paper.apply(titleChangedEvent);

        // When
        replicatorPaperRepository.save(paper);

        // Then
        Paper retrievedPaper = replicatorPaperRepository.getById(paper.getId());
        assertThat(retrievedPaper.getId(), is(equalTo(paper.getId())));
        assertThat(retrievedPaper.getTitle(), is(equalTo("Updated title")));
    }

    @Test
    public void shouldAllowUpdatingExistingPaperWithAnyGreaterVersionOfPaper() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        Paper paper = aPaper().withVersion(9).withTitle("Initial title").build();
        replicatorPaperRepository.save(paper);

        int versionThatIsAlphanumericallySmallerButNumericallyGreater = 11;
        Paper updatedPaper = aPaper().withId(paper.getId())
                .withVersion(versionThatIsAlphanumericallySmallerButNumericallyGreater)
                .withTitle("Updated title")
                .build();

        // When
        replicatorPaperRepository.save(updatedPaper);

        // Then
        Paper retrievedPaper = replicatorPaperRepository.getById(updatedPaper.getId());
        assertThat(retrievedPaper.getId(), is(equalTo(updatedPaper.getId())));
        assertThat(retrievedPaper.getTitle(), is(equalTo("Updated title")));
    }

    @Test
    public void shouldRetrievePaperFromDatabaseById() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        Paper privatePaper = aPaper()
                .withTitle("Papers title")
                .withVersion(12)
                .withPaperPrivate(true)
                .withPaperIrrelevant(false)
                .withAuthorIds("5", "3", "8")
                .withSubmissionStage(SUBMITTED)
                .build();

        Paper publicPaper = aPaper()
                .withPaperPrivate(false)
                .withPaperIrrelevant(false)
                .build();

        Paper irrelevantPaper = aPaper()
                .withPaperPrivate(false)
                .withPaperIrrelevant(true)
                .build();

        replicatorPaperRepository.save(asList(privatePaper, publicPaper, irrelevantPaper));

        // When
        Paper retrievedPrivatePaper = replicatorPaperRepository.getById(privatePaper.getId());

        // Then
        assertThat(retrievedPrivatePaper.getId(), is(equalTo(privatePaper.getId())));
        assertThat(retrievedPrivatePaper.getTitle(), is(equalTo("Papers title")));
        assertThat(retrievedPrivatePaper.getVersion(), is(equalTo(12)));
        assertThat(retrievedPrivatePaper.isPaperPrivate(), is(equalTo(true)));
        assertThat(retrievedPrivatePaper.isPaperIrrelevant(), is(equalTo(false)));
        assertThat(retrievedPrivatePaper.getAuthorIds(), is(equalTo(new String[]{"5", "3", "8"})));
        assertThat(retrievedPrivatePaper.getSubmissionStage(), is(equalTo(SUBMITTED)));

        // When
        Paper retrievedPublicPaper = replicatorPaperRepository.getById(publicPaper.getId());

        // Then
        assertThat(retrievedPublicPaper.getId(), is(equalTo(publicPaper.getId())));
        assertThat(retrievedPublicPaper.isPaperPrivate(), is(equalTo(false)));
        assertThat(retrievedPublicPaper.isPaperIrrelevant(), is(equalTo(false)));

        // When
        Paper retrievedIrrelevantPaper = replicatorPaperRepository.getById(irrelevantPaper.getId());

        // Then
        assertThat(retrievedIrrelevantPaper.getId(), is(equalTo(irrelevantPaper.getId())));
        assertThat(retrievedIrrelevantPaper.isPaperPrivate(), is(equalTo(false)));
        assertThat(retrievedIrrelevantPaper.isPaperIrrelevant(), is(equalTo(true)));
    }

    @Test
    public void shouldRecordInsertTimeOfAPaper() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        Paper paper = aPaper().build();

        // When
        replicatorPaperRepository.save(paper);
        DateTime insertTime = DateTime.now();

        // Then
        DateTime lastUpdatedTimestamp = postgresDatabase().getLastUpdatedTimeForPaper(paper);
        assertThat(lastUpdatedTimestamp, is(
                allOf(greaterThan(insertTime.minusMillis(250)), lessThan(insertTime.plusMillis(250)))
        ));
    }

    @Test
    public void shouldRecordUpdateTimeOfAPaper() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        Paper paper = aPaper().build();
        replicatorPaperRepository.save(paper);
        DateTime retrievedInsertTime = postgresDatabase().getLastUpdatedTimeForPaper(paper);

        // When
        replicatorPaperRepository.save(paper);
        DateTime updateTime = DateTime.now();

        // Then
        DateTime retrievedUpdateTime = postgresDatabase().getLastUpdatedTimeForPaper(paper);
        assertThat(retrievedUpdateTime, is(not(equalTo(retrievedInsertTime))));
        assertThat(retrievedUpdateTime, is(
                allOf(greaterThan(updateTime.minusMillis(250)), lessThan(updateTime.plusMillis(250)))
        ));
    }

    @Test
    public void shouldIndicateIfPaperExistsInDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        Paper paper = aPaper().build();
        assertThat(replicatorPaperRepository.hasPaper(paper.getId()), is(false));
        replicatorPaperRepository.save(paper);

        // When
        boolean hasPaper = replicatorPaperRepository.hasPaper(paper.getId());

        // Then
        assertThat(hasPaper, is(true));
    }

    @Test
    public void shouldThrowExceptionWhenAskedToRetrieveANonExistentPaper() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try {
            // When
            replicatorPaperRepository.getById("Non-Existent Paper ID");
            fail("Expected exception to be thrown");
        }
        // Then
        catch (PaperNotFoundException paperNotFoundException) {
            assertThat(paperNotFoundException.getPaperId(), is(equalTo("Non-Existent Paper ID")));
        }
    }

    @Test
    public void shouldNotSaveOlderVersionOfPaperToDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper = aPaper().withVersion(2).build();
            replicatorPaperRepository.save(paper);
            assertThat(() -> papers, eventuallySatisfies(hasItem(aPaperWith(id(String.valueOf(paper.getId())))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
            papers.clear();

            Paper previousVersionPaper = aPaper().withId(paper.getId()).withVersion(1).build();

            // When
            replicatorPaperRepository.save(previousVersionPaper);

            // Then
            sleepFor(1, SECONDS);
            assertThat(papers, is(empty()));

            Paper paperRetrievedFromDatabase = replicatorPaperRepository.getById(paper.getId());
            assertThat(paperRetrievedFromDatabase.getVersion(), is(equalTo(2)));
        }
    }

    @Test
    public void shouldSupportStreamingFromAnExistingReplicationSlot() {
        // Given

        PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);
        }

        // When
        try (PostgresPaperRepository anotherEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anotherEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));
            replicatorPostgresPaperRepository.save(aPaper().build());

            // Then
            assertThat(() -> papers, eventuallySatisfies(is(not(empty())))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotStreamPreviouslyStreamedPapersOnRestart() {
        // Given
        String firstPaperId = randomId();
        String secondPaperId = randomId();

        PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            replicatorPostgresPaperRepository.save(aPaper().withId(firstPaperId).build());
            replicatorPostgresPaperRepository.save(aPaper().withId(secondPaperId).build());

            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(id(firstPaperId)), aPaperWith(id(secondPaperId)))
            ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }

        // When
        try (PostgresPaperRepository anotherEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anotherEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            String thirdPaperId = randomId();
            replicatorPostgresPaperRepository.save(aPaper().withId(thirdPaperId).build());

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItem(aPaperWith(id(thirdPaperId))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            assertThat(papers, not(hasItems(
                    aPaperWith(id(firstPaperId)),
                    aPaperWith(id(secondPaperId))
            )));
        }
    }

    @Test
    public void shouldStreamPapersThatWereUpdatedInDatabaseBeforeRestart() {
        // Given
        PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);
        }

        String firstPaperId = randomId();
        String secondPaperId = randomId();

        replicatorPostgresPaperRepository.save(aPaper().withId(firstPaperId).build());
        replicatorPostgresPaperRepository.save(aPaper().withId(secondPaperId).build());

        try (PostgresPaperRepository anotherEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            // When
            onABackgroundThread(() -> anotherEventsEmitterPostgresPaperRepository.onPaperUpdated(papers::add));

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(id(firstPaperId)),
                    aPaperWith(id(secondPaperId))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotMoveAheadWhenAnExceptionIsThrownByPaperConsumer() {
        // Given
        try (PostgresPaperRepository anEventsEmitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Paper> papers = new CopyOnWriteArrayList<>();
            AtomicBoolean nextPaperSuccessfullyConsumed = new AtomicBoolean(true);
            onABackgroundThread(() -> anEventsEmitterPostgresPaperRepository.onPaperUpdated(paper -> {
                synchronized (nextPaperSuccessfullyConsumed) {
                    if (nextPaperSuccessfullyConsumed.get()) {
                        papers.add(paper);
                    } else {
                        nextPaperSuccessfullyConsumed.set(true);
                        throw new RuntimeException("Could not handle event");
                    }
                }
            }));

            PostgresPaperRepository replicatorPostgresPaperRepository = new PostgresPaperRepository(dbi);
            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPostgresPaperRepository);

            nextPaperSuccessfullyConsumed.set(false);

            // When
            String firstPaperId = randomId();
            replicatorPostgresPaperRepository.save(aPaper().withId(firstPaperId).build());

            String secondPaperId = randomId();
            replicatorPostgresPaperRepository.save(aPaper().withId(secondPaperId).build());

            // Then
            assertThat(() -> papers, eventuallySatisfies(containsInRelativeOrder(
                    aPaperWith(id(firstPaperId)),
                    aPaperWith(id(secondPaperId))
                    )
            ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyThatABatchOfPapersHaveBeenCreatedInTheSameOrderTheyWereInsertedInToTheDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper1 = aPaper().withTitle("First paper's title").build();

            Paper paper2 = aPaper().withTitle("Second paper's title").build();

            // When
            replicatorPaperRepository.save(asList(paper1, paper2));

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(id(String.valueOf(paper1.getId())), title(paper1.getTitle())),
                    aPaperWith(id(String.valueOf(paper2.getId())), title(paper2.getTitle()))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldFilterOutNullTerminatorCharactersFromEntityJsonWhenUpsertingABatchOfPapers() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper1 = aPaper().withTitle("First title containing null\u0000 terminator characters").build();

            Paper paper2 = aPaper().withTitle("Second title containing null\u0000 terminator characters").build();

            // When
            replicatorPaperRepository.save(asList(paper1, paper2));

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(id(String.valueOf(paper1.getId())), title("First title containing null terminator characters")),
                    aPaperWith(id(String.valueOf(paper2.getId())), title("Second title containing null terminator characters"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyThatBatchOfPapersHaveBeenUpdatedInOrderTheyWereUpdatedInDatabase() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper1 = aPaper().withVersion(1).build();
            Paper paper2 = aPaper().withVersion(1).build();
            replicatorPaperRepository.save(asList(paper1, paper2));

            List<Paper> updatedPapers = asList(
                    aPaper().withId(paper1.getId()).withVersion(2).build(),
                    aPaper().withId(paper2.getId()).withVersion(2).build()
            );

            // When
            replicatorPaperRepository.save(updatedPapers);

            // Then
            assertThat(() -> papers, eventuallySatisfies(hasItems(
                    aPaperWith(id(String.valueOf(paper1.getId())), version(2)),
                    aPaperWith(id(String.valueOf(paper2.getId())), version(2))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotCommitAnyChangesWhenABatchUpsertFails() {
        // Given
        PaperRepository replicatorPaperRepository = new PostgresPaperRepository(dbi);

        try (PostgresPaperRepository emitterPostgresPaperRepository = new PostgresPaperRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Paper> papers = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> emitterPostgresPaperRepository.onPaperUpdated(papers::add));

            ensureRepositoryStreamingPaperUpdatesInto(papers, replicatorPaperRepository);

            Paper paper1 = aPaper().withTitle("First paper's title").build();

            int idDatabaseColumnWidth = 50;
            String idThatWillOverflowDatabaseColumn = IntStream.range(0, idDatabaseColumnWidth + 1)
                    .mapToObj(Integer::toString).collect(Collectors.joining());

            Paper paper2 = aPaper().withId(idThatWillOverflowDatabaseColumn).withTitle("Second paper's title").build();

            // When
            try {
                replicatorPaperRepository.save(asList(paper1, paper2));
            } catch (Throwable ignored) {
            }

            // Then
            sleepFor(3, SECONDS);
            assertThat(papers, not(hasItem(aPaperWith(id(String.valueOf(paper1.getId())), title(paper1.getTitle())))));
            assertThat(papers, not(hasItem(aPaperWith(id(String.valueOf(paper2.getId())), title(paper2.getTitle())))));
        }
    }

    private static void ensureRepositoryStreamingPaperUpdatesInto(Collection<Paper> papers, PaperRepository paperRepository) {
        whilstRepeating(() -> paperRepository.save(aPaper().build()))
                .waitUntil(() -> papers.size() > 0)
                .forNoMoreThan(30, SECONDS);
    }

    private static String randomId() {
        return UUID.randomUUID().toString();
    }

}
