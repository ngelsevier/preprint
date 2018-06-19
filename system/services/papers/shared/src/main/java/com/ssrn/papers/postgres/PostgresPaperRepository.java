package com.ssrn.papers.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.PaperNotFoundException;
import com.ssrn.papers.domain.PaperRepository;
import com.ssrn.papers.postgres.test_decoding.TestDecodingLogicalReplicationSlotSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.PreparedBatch;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ssrn.papers.utils.CompletableFutureUtils.rethrowFirstExceptionThrownBy;
import static com.ssrn.shared.concurrency.RetryUtils.waitUntil;

public class PostgresPaperRepository implements PaperRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresPaperRepository.class);
    private static final String PAPER_UPSERT_SQL_STATEMENT = "INSERT INTO paper (id, entity) VALUES (:id, :entity::jsonb) " +
            "ON CONFLICT (id) DO UPDATE SET entity = EXCLUDED.entity, last_updated = now() " +
            "WHERE (EXCLUDED.entity->>'version')::int >= (paper.entity->>'version')::int";
    private static final int SECONDS_TO_WAIT_FOR_ALL_LISTENERS_TO_START_BEFORE_SHUTTING_DOWN = 10;

    private final DBI dbi;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService;
    private final PartitionedMd5Range partitionedMd5Range;
    private final Collection<PaperUpdateListener> paperUpdateListeners = new CopyOnWriteArrayList<>();
    private final JsonPaperDeserializer jsonPaperDeserializer;
    private final TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource;
    private final int secondsBetweenCheckingWorkerThreadsForExceptions;

    public PostgresPaperRepository(PostgresDatabaseClientConfiguration postgresDatabaseClientConfiguration, PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration, DBI dbi, int maxConcurrentPaperUpdates, int secondsBetweenCheckingWorkerThreadsForExceptions) {
        this(dbi, Executors.newFixedThreadPool(maxConcurrentPaperUpdates), postgresReplicationStreamingConfiguration, new PartitionedMd5Range(maxConcurrentPaperUpdates), new PostgresDatabaseClient(postgresDatabaseClientConfiguration), secondsBetweenCheckingWorkerThreadsForExceptions);
    }

    public PostgresPaperRepository(DBI dbi) {
        this(dbi, null, null, null, null, Integer.MAX_VALUE);
    }

    private PostgresPaperRepository(DBI dbi, ExecutorService executorService, PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration, PartitionedMd5Range partitionedMd5Range, PostgresDatabaseClient postgresDatabaseClient, int secondsBetweenCheckingWorkerThreadsForExceptions) {
        this.dbi = dbi;
        this.executorService = executorService;
        this.jsonPaperDeserializer = new JsonPaperDeserializer(objectMapper);
        this.partitionedMd5Range = partitionedMd5Range;
        this.secondsBetweenCheckingWorkerThreadsForExceptions = secondsBetweenCheckingWorkerThreadsForExceptions;

        testDecodingLogicalReplicationSlotSource = postgresDatabaseClient == null ? null :
                new TestDecodingLogicalReplicationSlotSource(dbi, postgresDatabaseClient, postgresReplicationStreamingConfiguration);
    }

    public void onPaperUpdated(Consumer<Paper> paperConsumer) {
        int partitionCount = partitionedMd5Range.getPartitions().size();

        testDecodingLogicalReplicationSlotSource.getSlots().stream()
                .filter(slot -> slot.getIndex() >= partitionCount)
                .forEach(LogicalReplicationSlot::drop);

        List<CompletableFuture<Object>> paperUpdateListenerFutures = partitionedMd5Range.getPartitions().stream()
                .map(partition -> CompletableFuture.supplyAsync(() -> {
                    PaperUpdateListener paperUpdateListener = createPaperUpdateListenerFor(partition);

                    paperUpdateListener.onPaperUpdated(paper -> {
                        if (partition.contains(paper.getId())) {
                            paperConsumer.accept(paper);
                        }
                    });

                    return null;
                }, executorService))
                .collect(Collectors.toList());

        rethrowFirstExceptionThrownBy(paperUpdateListenerFutures, secondsBetweenCheckingWorkerThreadsForExceptions);
    }

    @Override
    public void close() {
        try {
            waitUntil(() -> paperUpdateListeners.size() == partitionedMd5Range.getPartitions().size(),
                    () -> LOGGER.warn(String.format("Timed out waiting for actual number of listeners to match expected number of listeners before proceeding to close them. Waited %s seconds.", SECONDS_TO_WAIT_FOR_ALL_LISTENERS_TO_START_BEFORE_SHUTTING_DOWN)),
                    SECONDS_TO_WAIT_FOR_ALL_LISTENERS_TO_START_BEFORE_SHUTTING_DOWN);

            paperUpdateListeners.forEach(PaperUpdateListener::close);

            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public Paper getById(String id) {
        Paper paper = fetchPaper(id);

        if (paper != null) {
            return paper;
        }

        throw new PaperNotFoundException(id);
    }

    @Override
    public boolean hasPaper(String id) {
        return fetchPaper(id) != null;

    }

    @Override
    public void save(Paper paper) {
        dbi.withHandle(handle -> {
            handle.createStatement(PAPER_UPSERT_SQL_STATEMENT)
                    .bind("id", paper.getId())
                    .bind("entity", removeNullTerminatorCharactersFrom(jsonRepresentationOf(paper)))
                    .execute();
            return null;
        });
    }

    @Override
    public void save(List<Paper> papers) {
        dbi.withHandle(handle -> {
            PreparedBatch preparedBatch = handle.prepareBatch(PAPER_UPSERT_SQL_STATEMENT);
            papers.forEach(paper -> preparedBatch.bind("id", paper.getId())
                    .bind("entity", removeNullTerminatorCharactersFrom(jsonRepresentationOf(paper)))
                    .add());

            preparedBatch.execute();
            return null;
        });
    }

    private Paper fetchPaper(String id) {
        return dbi.withHandle(handle -> handle.createQuery("SELECT entity FROM paper WHERE id = :id")
                .bind("id", id)
                .map((index, resultSet, statementContext) -> jsonPaperDeserializer.deserializePaperFromJson(resultSet.getString("entity")))
                .first());
    }

    private PaperUpdateListener createPaperUpdateListenerFor(Md5BasedPartition partition) {
        PaperUpdateListener paperUpdateListener = new PaperUpdateListener(
                partition.getPartitionIndex(),
                jsonPaperDeserializer,
                testDecodingLogicalReplicationSlotSource
        );

        paperUpdateListeners.add(paperUpdateListener);
        return paperUpdateListener;
    }

    private String jsonRepresentationOf(Paper paper) {
        try {
            return objectMapper.writeValueAsString(paper);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object removeNullTerminatorCharactersFrom(String text) {
        return text == null ? null : text.replace("\\u0000", "");
    }

}
