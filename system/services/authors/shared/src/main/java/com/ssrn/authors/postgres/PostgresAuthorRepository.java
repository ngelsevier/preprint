package com.ssrn.authors.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorNotFoundException;
import com.ssrn.authors.domain.AuthorRepository;
import com.ssrn.authors.postgres.test_decoding.TestDecodingLogicalReplicationSlotSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ssrn.authors.utils.CompletableFutureUtils.rethrowFirstExceptionThrownBy;
import static com.ssrn.shared.concurrency.RetryUtils.waitUntil;

public class PostgresAuthorRepository implements AuthorRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresAuthorRepository.class);
    private static final int SECONDS_TO_WAIT_FOR_ALL_LISTENERS_TO_START_BEFORE_SHUTTING_DOWN = 10;
    private static final String AUTHOR_UPSERT_SQL_STATEMENT = "INSERT INTO author (id, entity) VALUES (:id, :entity::jsonb) " +
            "ON CONFLICT (id) DO UPDATE SET entity = EXCLUDED.entity, last_updated = now() " +
            "WHERE (EXCLUDED.entity->>'version')::int >= (author.entity->>'version')::int";

    private final DBI dbi;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService;
    private final PartitionedMd5Range partitionedMd5Range;
    private final Collection<AuthorUpdateListener> authorUpdateListeners = new CopyOnWriteArrayList<>();
    private final JsonAuthorDeserializer jsonAuthorDeserializer;
    private final TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource;
    private final int secondsBetweenCheckingWorkerThreadsForExceptions;

    public PostgresAuthorRepository(PostgresDatabaseClientConfiguration postgresDatabaseClientConfiguration, PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration, DBI dbi, int maxConcurrentAuthorUpdates, int secondsBetweenCheckingWorkerThreadsForExceptions) {
        this(dbi, Executors.newFixedThreadPool(maxConcurrentAuthorUpdates), postgresReplicationStreamingConfiguration, new PartitionedMd5Range(maxConcurrentAuthorUpdates), new PostgresDatabaseClient(postgresDatabaseClientConfiguration), secondsBetweenCheckingWorkerThreadsForExceptions);
    }

    public PostgresAuthorRepository(DBI dbi) {
        this(dbi, null, null, null, null, Integer.MAX_VALUE);
    }

    private PostgresAuthorRepository(DBI dbi, ExecutorService executorService, PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration, PartitionedMd5Range partitionedMd5Range, PostgresDatabaseClient postgresDatabaseClient, int secondsBetweenCheckingWorkerThreadsForExceptions) {
        this.dbi = dbi;
        this.executorService = executorService;
        this.jsonAuthorDeserializer = new JsonAuthorDeserializer(objectMapper);
        this.partitionedMd5Range = partitionedMd5Range;
        this.secondsBetweenCheckingWorkerThreadsForExceptions = secondsBetweenCheckingWorkerThreadsForExceptions;

        testDecodingLogicalReplicationSlotSource = postgresDatabaseClient == null ? null :
                new TestDecodingLogicalReplicationSlotSource(dbi, postgresDatabaseClient, postgresReplicationStreamingConfiguration);
    }

    public void onAuthorUpdated(Consumer<Author> authorConsumer) {
        int partitionCount = partitionedMd5Range.getPartitions().size();

        testDecodingLogicalReplicationSlotSource.getSlots().stream()
                .filter(slot -> slot.getIndex() >= partitionCount)
                .forEach(LogicalReplicationSlot::drop);

        List<CompletableFuture<Object>> authorUpdateListenerFutures = partitionedMd5Range.getPartitions().stream()
                .map(partition -> CompletableFuture.supplyAsync(() -> {
                    AuthorUpdateListener authorUpdateListener = createAuthorUpdateListenerFor(partition);

                    authorUpdateListener.onAuthorUpdated(author -> {
                        if (partition.contains(author.getId())) {
                            authorConsumer.accept(author);
                        }
                    });

                    return null;
                }, executorService))
                .collect(Collectors.toList());

        rethrowFirstExceptionThrownBy(authorUpdateListenerFutures, secondsBetweenCheckingWorkerThreadsForExceptions);
    }

    @Override
    public void close() {
        try {
            waitUntil(() -> authorUpdateListeners.size() == partitionedMd5Range.getPartitions().size(),
                    () -> LOGGER.warn(String.format("Timed out waiting for actual number of listeners to match expected number of listeners before proceeding to close them. Waited %s seconds.", SECONDS_TO_WAIT_FOR_ALL_LISTENERS_TO_START_BEFORE_SHUTTING_DOWN)),
                    SECONDS_TO_WAIT_FOR_ALL_LISTENERS_TO_START_BEFORE_SHUTTING_DOWN);

            authorUpdateListeners.forEach(AuthorUpdateListener::close);

            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public Author getById(String id) {
        Author author = fetchAuthor(id);
        if (author != null) {
            return author;
        }
        throw new AuthorNotFoundException();
    }

    @Override
    public boolean hasAuthor(String id) {
        return fetchAuthor(id) != null;

    }

    @Override
    public void save(Author author) {
        dbi.withHandle(handle -> {
            handle.createStatement(AUTHOR_UPSERT_SQL_STATEMENT)
                    .bind("id", author.getId())
                    .bind("entity", removeNullTerminatorCharactersFrom(jsonRepresentationOf(author)))
                    .execute();
            return null;
        });
    }

    @Override
    public void save(List<Author> authors) {
        dbi.withHandle(handle -> {
            PreparedBatch preparedBatch = handle.prepareBatch(AUTHOR_UPSERT_SQL_STATEMENT);
            authors.forEach(author -> preparedBatch.bind("id", author.getId())
                    .bind("entity", removeNullTerminatorCharactersFrom(jsonRepresentationOf(author)))
                    .add());

            preparedBatch.execute();
            return null;
        });
    }

    private Author fetchAuthor(String id) {
        return dbi.withHandle(handle -> handle.createQuery("SELECT entity FROM author WHERE id = :id")
                .bind("id", id)
                .map((index, resultSet, statementContext) -> jsonAuthorDeserializer.deserializeAuthorFromJson(resultSet.getString("entity")))
                .first());
    }

    private AuthorUpdateListener createAuthorUpdateListenerFor(Md5BasedPartition partition) {
        AuthorUpdateListener authorUpdateListener = new AuthorUpdateListener(
                partition.getPartitionIndex(),
                jsonAuthorDeserializer,
                testDecodingLogicalReplicationSlotSource
        );

        authorUpdateListeners.add(authorUpdateListener);
        return authorUpdateListener;
    }

    private String jsonRepresentationOf(Author author) {
        try {
            return objectMapper.writeValueAsString(author);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object removeNullTerminatorCharactersFrom(String text) {
        return text == null ? null : text.replace("\\u0000", "");
    }

}
