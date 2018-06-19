package com.ssrn.authors.postgres;

import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorNotFoundException;
import com.ssrn.authors.domain.AuthorRepository;
import com.ssrn.authors.domain.Event;
import com.ssrn.authors.postgres.support.PartitionedMd5Range;
import com.ssrn.authors.shared.test_support.postgres.PostgresAuthorsDatabaseIntegrationTest;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ssrn.authors.postgres.support.PartitionedMd5Range.partitionedMd5Range;
import static com.ssrn.authors.shared.test_support.entity.AuthorBuilder.anAuthor;
import static com.ssrn.authors.shared.test_support.event.EventBuilder.anEvent;
import static com.ssrn.authors.shared.test_support.matchers.AuthorMatcher.anAuthorWith;
import static com.ssrn.authors.shared.test_support.matchers.AuthorWithIdMatcher.id;
import static com.ssrn.authors.shared.test_support.matchers.AuthorWithNameMatcher.name;
import static com.ssrn.authors.shared.test_support.matchers.AuthorWithRemovalMatcher.removed;
import static com.ssrn.authors.shared.test_support.matchers.AuthorWithVersionMatcher.version;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.RepeaterFluentSyntax.repeat;
import static com.ssrn.test.support.utils.TaskWhilstAnotherTaskRepeats.whilstRepeating;
import static com.ssrn.test.support.utils.ThreadingUtils.onABackgroundThread;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

public class PostgresAuthorRepositoryTest extends PostgresAuthorsDatabaseIntegrationTest {

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
                postgresDatabase().getEventsPublisherUsername(),
                postgresDatabase().getEventsPublisherPassword(),
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
    public void shouldNotifyThatAnAuthorIsUpdatedWhenANewAuthorIsSavedToTheDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author = anAuthor().withName("Author's name").withRemoval(false).build();

            // When
            replicatorAuthorRepository.save(author);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItem(anAuthorWith(id(author.getId()), name("Author's name"), removed(author.isRemoved()))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
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

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(impatientConfiguration, postgresReplicationStreamingConfiguration, dbi, 4, 1)) {
            publisherPostgresAuthorRepository.onAuthorUpdated(ignored -> {
            });
        }
    }

    @Test
    public void shouldRecoverFromLossOfReplicationConnection() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

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


        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(impatientDatabaseClientConfiguration, impatientReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author = anAuthor().withName("Author's name").build();

            // When
            postgresDatabase().ensureStopped();
            sleepFor(6, SECONDS);
            postgresDatabase().ensureStarted();
            replicatorAuthorRepository.save(author);
            assertThat(authors, not(hasItem(anAuthorWith(id(author.getId()), name("Author's name")))));

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItem(anAuthorWith(id(author.getId()), name("Author's name"))))
                    .within(45, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldRaiseEachAuthorUpdateNotificationsOnOneOfASpecifiedNumberOfThreads() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        int maxConcurrentAuthorUpdates = 2;

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, maxConcurrentAuthorUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();
            List<String> notifiedAuthorIds = new CopyOnWriteArrayList<>();

            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(author -> {
                uniqueNotificationThreadIds.add(Thread.currentThread().getId());
                notifiedAuthorIds.add(author.getId());
            }));

            PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(maxConcurrentAuthorUpdates);

            whilstRepeating(() -> {
                String[] partitionedAuthorIds = IntStream.range(0, maxConcurrentAuthorUpdates)
                        .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                        .toArray(String[]::new);
                Arrays.stream(partitionedAuthorIds).forEach(authorId -> replicatorAuthorRepository.save(anAuthor().withId(authorId).build()));
            })
                    .waitUntil(() -> uniqueNotificationThreadIds.size() == maxConcurrentAuthorUpdates)
                    .forNoMoreThan(60, SECONDS);

            String[] morePartitionedAuthorIds = IntStream.range(0, maxConcurrentAuthorUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new);

            // When
            Arrays.stream(morePartitionedAuthorIds)
                    .forEach(authorId -> replicatorAuthorRepository.save(anAuthor().withId(authorId).build()));

            // Then
            assertThat(() -> notifiedAuthorIds, eventuallySatisfies(hasItems(morePartitionedAuthorIds))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            Arrays.stream(morePartitionedAuthorIds)
                    .forEach(authorId -> assertThat(notifiedAuthorIds.stream().filter(id -> id.equals(authorId)).count(), is(equalTo(1L))));

            assertThat(uniqueNotificationThreadIds, hasSize(maxConcurrentAuthorUpdates));
        }
    }

    @Test
    public void shouldDropAnySuperfluousSlotsBeforeListeningForAuthorUpdateNotifications() {
        // Given

        PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);

        int initialMaxConcurrentAuthorUpdates = 4;

        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, initialMaxConcurrentAuthorUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();

            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(p -> uniqueNotificationThreadIds.add(Thread.currentThread().getId())));

            PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(initialMaxConcurrentAuthorUpdates);

            whilstRepeating(() -> Arrays.stream(IntStream.range(0, initialMaxConcurrentAuthorUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new)).forEach(authorId -> replicatorPostgresAuthorRepository.save(anAuthor().withId(authorId).build())))
                    .waitUntil(() -> uniqueNotificationThreadIds.size() == initialMaxConcurrentAuthorUpdates)
                    .forNoMoreThan(30, SECONDS);

            assertThat(postgresDatabase().replicationSlotCount(), is(equalTo(4)));
        }

        // When
        int reducedMaxConcurrentAuthorUpdates = 2;

        try (PostgresAuthorRepository anotherEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, reducedMaxConcurrentAuthorUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();

            onABackgroundThread(() -> anotherEventsPublisherPostgresAuthorRepository.onAuthorUpdated(p -> uniqueNotificationThreadIds.add(Thread.currentThread().getId())));

            PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(reducedMaxConcurrentAuthorUpdates);

            whilstRepeating(() -> Arrays.stream(IntStream.range(0, reducedMaxConcurrentAuthorUpdates)
                    .mapToObj(partitionedMd5Range::getRandomStringInPartition)
                    .toArray(String[]::new)).forEach(authorId -> replicatorPostgresAuthorRepository.save(anAuthor().withId(authorId).build())))
                    .waitUntil(() -> uniqueNotificationThreadIds.size() == reducedMaxConcurrentAuthorUpdates)
                    .forNoMoreThan(30, SECONDS);

            assertThat(postgresDatabase().replicationSlotCount(), is(equalTo(2)));
        }
    }

    @Test
    public void shouldRaiseAllAuthorUpdateNotificationsForAGivenAuthorOnTheSameThread() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        int maxConcurrentAuthorUpdates = 2;

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, maxConcurrentAuthorUpdates, 1)) {
            Set<Long> uniqueNotificationThreadIds = new CopyOnWriteArraySet<>();
            List<String> notifiedAuthorIds = new CopyOnWriteArrayList<>();

            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(p -> {
                uniqueNotificationThreadIds.add(Thread.currentThread().getId());
                notifiedAuthorIds.add(p.getId());
            }));

            whilstRepeating(() -> replicatorAuthorRepository.save(anAuthor().withId("10").build()))
                    .waitUntil(() -> notifiedAuthorIds.size() > 0)
                    .forNoMoreThan(30, SECONDS);

            // When
            repeat(() -> replicatorAuthorRepository.save(anAuthor().withId("10").build()))
                    .times(50);

            // Then
            assertThat(() -> notifiedAuthorIds, eventuallySatisfies(hasSize(greaterThanOrEqualTo(50)))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            assertThat(uniqueNotificationThreadIds, hasSize(1));
        }
    }

    @Test
    public void shouldNotifyOfAuthorUpdatesInOrderTheyOccur() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            // When
            replicatorAuthorRepository.save(anAuthor().withId("1").build());
            replicatorAuthorRepository.save(anAuthor().withId("2").build());
            replicatorAuthorRepository.save(anAuthor().withId("3").build());

            // Then
            assertThat(() -> authors, eventuallySatisfies(containsInRelativeOrder(
                    anAuthorWith(id("1")),
                    anAuthorWith(id("2")),
                    anAuthorWith(id("3"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyThatAnAuthorIsUpdatedWhenAnExistingAuthorIsSavedToTheDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository PublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> PublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author = anAuthor().withName("Initial name").build();
            replicatorAuthorRepository.save(author);

            Event nameChangedEvent = anEvent().withEntityVersion(author.getVersion() + 1).withType("NAME CHANGED").withData(new JSONObject().put("name", "Updated name")).build();
            author.apply(nameChangedEvent);

            // When
            replicatorAuthorRepository.save(author);

            // Then
            assertThat(() -> authors, eventuallySatisfies(Matchers.hasItem(anAuthorWith(id(author.getId()), name("Updated name"))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyOfRepeatedSavesOfTheSameAuthor() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository PublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> PublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author = anAuthor().withVersion(2).build();

            replicatorAuthorRepository.save(author);

            // When
            replicatorAuthorRepository.save(author);

            // Then
            assertThat(() -> authors, eventuallySatisfies(containsInRelativeOrder(
                    anAuthorWith(id(author.getId())),
                    anAuthorWith(id(author.getId()))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingSingleQuotes() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            String name = String.format("Author's name with single quote %s", UUID.randomUUID());
            Author author = anAuthor().withName(name).build();

            // When
            replicatorAuthorRepository.save(author);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItem(anAuthorWith(id(author.getId()), name(name))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingUtf8Characters() {
        // Given
        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            String expectedName = "The Name contains some UTF-8 characters like Фінансово";
            Author authorWithUtf8CharactersInName = anAuthor().withName(expectedName).build();

            // When
            replicatorPostgresAuthorRepository.save(authorWithUtf8CharactersInName);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(name(expectedName))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingUnicodeLineSeparatorsCharacters() {
        // Given
        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            String wonkyName = "The Name contains a\u2028line separator";
            Author authorWithUnicodeLineSeparatorInName = anAuthor().withName(wonkyName).build();

            // When
            replicatorPostgresAuthorRepository.save(authorWithUnicodeLineSeparatorInName);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(name(wonkyName))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldSupportEntityJsonContainingLineBreaks() {
        // Given
        try (PostgresAuthorRepository aPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> aPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            Author authorWithLineBreakInName = anAuthor().withName("The Name contains a\nline break").build();

            // When
            replicatorPostgresAuthorRepository.save(authorWithLineBreakInName);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(name("The Name contains a\nline break"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldFilterOutNullTerminatorCharactersFromEntityJson() {
        // Given
        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            Author authorWithNullTerminatorCharacterInName = anAuthor().withName("a name containing null\u0000 terminator characters").build();

            // When
            replicatorPostgresAuthorRepository.save(authorWithNullTerminatorCharacterInName);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(name("a name containing null terminator characters"))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldUpdateExistingAuthorInDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        Author author = anAuthor().withName("Initial name").build();
        replicatorAuthorRepository.save(author);

        Event nameChangedEvent = anEvent().withEntityVersion(author.getVersion() + 1).withType("NAME CHANGED").withData(new JSONObject().put("name", "Updated name")).build();
        author.apply(nameChangedEvent);

        // When
        replicatorAuthorRepository.save(author);

        // Then
        Author retrievedAuthor = replicatorAuthorRepository.getById(author.getId());
        assertThat(retrievedAuthor.getId(), is(equalTo(author.getId())));
        assertThat(retrievedAuthor.getName(), is(equalTo("Updated name")));
    }

    @Test
    public void shouldAllowUpdatingExistingAuthorWithAnyGreaterVersionOfAuthor() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        Author author = anAuthor().withVersion(9).withName("Initial name").build();
        replicatorAuthorRepository.save(author);

        int versionThatIsAlphanumericallySmallerButNumericallyGreater = 11;
        Author updatedAuthor = anAuthor().withId(author.getId())
                .withVersion(versionThatIsAlphanumericallySmallerButNumericallyGreater)
                .withName("Updated name")
                .build();

        // When
        replicatorAuthorRepository.save(updatedAuthor);

        // Then
        Author retrievedAuthor = replicatorAuthorRepository.getById(updatedAuthor.getId());
        assertThat(retrievedAuthor.getId(), is(equalTo(updatedAuthor.getId())));
        assertThat(retrievedAuthor.getName(), is(equalTo("Updated name")));
    }

    @Test
    public void shouldRetrieveAuthorFromDatabaseById() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        String name = "Authors name";
        Author author = anAuthor().withName(name).withVersion(12).build();
        replicatorAuthorRepository.save(author);

        // When
        Author retrievedAuthor = replicatorAuthorRepository.getById(author.getId());

        // Then
        assertThat(retrievedAuthor.getId(), is(equalTo(author.getId())));
        assertThat(retrievedAuthor.getName(), is(equalTo(name)));
        assertThat(retrievedAuthor.getVersion(), is(equalTo(12)));
    }

    @Test
    public void shouldRecordInsertTimeOfAAuthor() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        Author author = anAuthor().build();

        // When
        replicatorAuthorRepository.save(author);
        DateTime insertTime = DateTime.now();

        // Then
        DateTime lastUpdatedTimestamp = postgresDatabase().getLastUpdatedTimeForAuthor(author);
        assertThat(lastUpdatedTimestamp, is(
                allOf(greaterThan(insertTime.minusMillis(250)), lessThan(insertTime.plusMillis(250)))
        ));
    }

    @Test
    public void shouldRecordUpdateTimeOfAAuthor() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        Author author = anAuthor().build();
        replicatorAuthorRepository.save(author);
        DateTime retrievedInsertTime = postgresDatabase().getLastUpdatedTimeForAuthor(author);

        // When
        replicatorAuthorRepository.save(author);
        DateTime updateTime = DateTime.now();

        // Then
        DateTime retrievedUpdateTime = postgresDatabase().getLastUpdatedTimeForAuthor(author);
        assertThat(retrievedUpdateTime, is(not(equalTo(retrievedInsertTime))));
        assertThat(retrievedUpdateTime, is(
                allOf(greaterThan(updateTime.minusMillis(250)), lessThan(updateTime.plusMillis(250)))
        ));
    }

    @Test
    public void shouldIndicateIfAuthorExistsInDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        Author author = anAuthor().build();
        assertThat(replicatorAuthorRepository.hasAuthor(author.getId()), is(false));
        replicatorAuthorRepository.save(author);

        // When
        boolean hasAuthor = replicatorAuthorRepository.hasAuthor(author.getId());

        // Then
        assertThat(hasAuthor, is(true));
    }

    @Test(expected = AuthorNotFoundException.class)
    public void shouldThrowExceptionWhenAskedToRetrieveANonExistentAuthor() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        // When
        replicatorAuthorRepository.getById("Non-Existent Author ID");

        // Then throw exception
    }

    @Test
    public void shouldNotSaveOlderVersionOfAuthorToDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author = anAuthor().withVersion(2).build();
            replicatorAuthorRepository.save(author);
            assertThat(() -> authors, eventuallySatisfies(hasItem(anAuthorWith(id(author.getId()))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
            authors.clear();

            Author previousVersionAuthor = anAuthor().withId(author.getId()).withVersion(1).build();

            // When
            replicatorAuthorRepository.save(previousVersionAuthor);

            // Then
            sleepFor(1, SECONDS);
            assertThat(authors, is(empty()));

            Author authorRetrievedFromDatabase = replicatorAuthorRepository.getById(author.getId());
            assertThat(authorRetrievedFromDatabase.getVersion(), is(equalTo(2)));
        }
    }

    @Test
    public void shouldSupportStreamingFromAnExistingReplicationSlot() {
        // Given

        PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);
        }

        // When
        try (PostgresAuthorRepository anotherEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anotherEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));
            replicatorPostgresAuthorRepository.save(anAuthor().build());

            // Then
            assertThat(() -> authors, eventuallySatisfies(is(not(empty())))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotStreamPreviouslyStreamedAuthorsOnRestart() {
        // Given
        String firstAuthorId = randomId();
        String secondAuthorId = randomId();

        PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            replicatorPostgresAuthorRepository.save(anAuthor().withId(firstAuthorId).build());
            replicatorPostgresAuthorRepository.save(anAuthor().withId(secondAuthorId).build());

            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(id(firstAuthorId)), anAuthorWith(id(secondAuthorId)))
            ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }

        // When
        try (PostgresAuthorRepository anotherEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anotherEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            String thirdAuthorId = randomId();
            replicatorPostgresAuthorRepository.save(anAuthor().withId(thirdAuthorId).build());

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItem(anAuthorWith(id(thirdAuthorId))))
                    .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

            assertThat(authors, not(hasItems(
                    anAuthorWith(id(firstAuthorId)),
                    anAuthorWith(id(secondAuthorId))
            )));
        }
    }

    @Test
    public void shouldStreamAuthorsThatWereUpdatedInDatabaseBeforeRestart() {
        // Given
        PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository anEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> anEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);
        }

        String firstAuthorId = randomId();
        String secondAuthorId = randomId();

        replicatorPostgresAuthorRepository.save(anAuthor().withId(firstAuthorId).build());
        replicatorPostgresAuthorRepository.save(anAuthor().withId(secondAuthorId).build());

        try (PostgresAuthorRepository anotherEventsPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            // When
            onABackgroundThread(() -> anotherEventsPublisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(id(firstAuthorId)),
                    anAuthorWith(id(secondAuthorId))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotMoveAheadWhenAnExceptionIsThrownByAuthorConsumer() {
        // Given
        try (PostgresAuthorRepository aPublisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            Collection<Author> authors = new CopyOnWriteArrayList<>();
            AtomicBoolean nextAuthorSuccessfullyConsumed = new AtomicBoolean(true);
            onABackgroundThread(() -> aPublisherPostgresAuthorRepository.onAuthorUpdated(author -> {
                synchronized (nextAuthorSuccessfullyConsumed) {
                    if (nextAuthorSuccessfullyConsumed.get()) {
                        authors.add(author);
                    } else {
                        nextAuthorSuccessfullyConsumed.set(true);
                        throw new RuntimeException("Could not handle event");
                    }
                }
            }));

            PostgresAuthorRepository replicatorPostgresAuthorRepository = new PostgresAuthorRepository(dbi);
            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorPostgresAuthorRepository);

            nextAuthorSuccessfullyConsumed.set(false);

            // When
            String firstAuthorId = randomId();
            replicatorPostgresAuthorRepository.save(anAuthor().withId(firstAuthorId).build());

            String secondAuthorId = randomId();
            replicatorPostgresAuthorRepository.save(anAuthor().withId(secondAuthorId).build());

            // Then
            assertThat(() -> authors, eventuallySatisfies(containsInRelativeOrder(
                    anAuthorWith(id(firstAuthorId)),
                    anAuthorWith(id(secondAuthorId))
                    )
            ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyThatABatchOfAuthorsHaveBeenCreatedInTheSameOrderTheyWereInsertedInToTheDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author1 = anAuthor().withName("First author's name").withRemoval(false).build();

            Author author2 = anAuthor().withName("Second author's name").withRemoval(true).build();

            // When
            replicatorAuthorRepository.save(asList(author1, author2));

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(id(author1.getId()), name(author1.getName()), removed(false)),
                    anAuthorWith(id(author2.getId()), name(author2.getName()), removed(true))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldFilterOutNullTerminatorCharactersFromEntityJsonWhenUpsertingABatchOfAuthors() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author authorWithNonRemoved = anAuthor().withName("First name containing null\u0000 terminator characters").withRemoval(false).build();

            Author authorToBeDeleted = anAuthor().withName("Second name containing null\u0000 terminator characters").withRemoval(true).build();

            // When
            replicatorAuthorRepository.save(asList(authorWithNonRemoved, authorToBeDeleted));

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(id(authorWithNonRemoved.getId()), name("First name containing null terminator characters"), removed(false)),
                    anAuthorWith(id(authorToBeDeleted.getId()), name("Second name containing null terminator characters"), removed(true))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotifyThatBatchOfAuthorsHaveBeenUpdatedInOrderTheyWereUpdatedInDatabase() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author1 = anAuthor().withVersion(1).build();
            Author author2 = anAuthor().withVersion(1).build();
            replicatorAuthorRepository.save(asList(author1, author2));

            List<Author> updatedAuthors = asList(
                    anAuthor().withId(author1.getId()).withVersion(2).build(),
                    anAuthor().withId(author2.getId()).withVersion(2).build()
            );

            // When
            replicatorAuthorRepository.save(updatedAuthors);

            // Then
            assertThat(() -> authors, eventuallySatisfies(hasItems(
                    anAuthorWith(id(author1.getId()), version(2)),
                    anAuthorWith(id(author2.getId()), version(2))
            )).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotCommitAnyChangesWhenABatchUpsertFails() {
        // Given
        AuthorRepository replicatorAuthorRepository = new PostgresAuthorRepository(dbi);

        try (PostgresAuthorRepository publisherPostgresAuthorRepository = new PostgresAuthorRepository(postgresDatabaseClientConfiguration, postgresReplicationStreamingConfiguration, dbi, 1, 1)) {
            List<Author> authors = new CopyOnWriteArrayList<>();
            onABackgroundThread(() -> publisherPostgresAuthorRepository.onAuthorUpdated(authors::add));

            ensureRepositoryStreamingAuthorUpdatesInto(authors, replicatorAuthorRepository);

            Author author1 = anAuthor().withName("First author's name").build();

            int idDatabaseColumnWidth = 50;
            String idThatWillOverflowDatabaseColumn = IntStream.range(0, idDatabaseColumnWidth + 1)
                    .mapToObj(Integer::toString).collect(Collectors.joining());

            Author author2 = anAuthor().withId(idThatWillOverflowDatabaseColumn).withName("Second author's name").build();

            // When
            try {
                replicatorAuthorRepository.save(asList(author1, author2));
            } catch (Throwable ignored) {
            }

            // Then
            sleepFor(3, SECONDS);
            assertThat(authors, not(hasItem(anAuthorWith(id(author1.getId()), name(author1.getName())))));
            assertThat(authors, not(hasItem(anAuthorWith(id(author2.getId()), name(author2.getName())))));
        }
    }

    private static void ensureRepositoryStreamingAuthorUpdatesInto(Collection<Author> authors, AuthorRepository authorRepository) {
        whilstRepeating(() -> authorRepository.save(anAuthor().build()))
                .waitUntil(() -> authors.size() > 0)
                .forNoMoreThan(60, SECONDS);
    }

    private static String randomId() {
        return UUID.randomUUID().toString();
    }

}
