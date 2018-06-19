package com.ssrn.authors.postgres.test_decoding;

import com.ssrn.authors.postgres.*;
import org.postgresql.core.BaseConnection;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.util.IntegerColumnMapper;

import java.sql.SQLException;

import static com.ssrn.authors.postgres.Interval.checkingEvery;
import static com.ssrn.authors.postgres.PostgresUtils.databaseObjectInUseCaused;
import static com.ssrn.authors.postgres.RetryingRunnable.tryAndRun;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TestDecodingLogicalReplicationSlot implements LogicalReplicationSlot {

    private final String slotName;
    private final PostgresDatabaseClient postgresDatabaseClient;
    private final int index;
    private final PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration;
    private final DBI dbi;

    public TestDecodingLogicalReplicationSlot(String slotName, int index, PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration, DBI dbi, PostgresDatabaseClient postgresDatabaseClient) {
        this.index = index;
        this.slotName = slotName;
        this.postgresDatabaseClient = postgresDatabaseClient;
        this.postgresReplicationStreamingConfiguration = postgresReplicationStreamingConfiguration;
        this.dbi = dbi;
    }

    @Override
    public ReplicationSlotStream createStream() {
        ensureTestDecodingReplicationSlotExists();

        return new ReplicationSlotStream(
                slotName,
                postgresReplicationStreamingConfiguration.getReplicationStreamCreationAttemptsTimeout(),
                postgresReplicationStreamingConfiguration.getReplicationStreamCreationAttemptsTimeoutUnit(),
                postgresReplicationStreamingConfiguration.getReplicationStreamStatusInterval(),
                postgresReplicationStreamingConfiguration.getReplicationStreamStatusIntervalUnits(),
                postgresDatabaseClient
        );
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void drop() {
        try (BaseConnection replicationConnection = postgresDatabaseClient.createReplicationConnection()) {
            tryAndRun(() -> {
                try {
                    replicationConnection.getReplicationAPI().dropReplicationSlot(slotName);
                } catch (SQLException e) {
                    if (databaseObjectInUseCaused(e)) {
                        throw new DatabaseObjectInUseException();
                    }

                    throw new RuntimeException(e);
                }
            }).retryingOn(DatabaseObjectInUseException.class)
                    .forNoMoreThan(postgresReplicationStreamingConfiguration.getReplicationSlotDropAttemptsTimeout(), postgresReplicationStreamingConfiguration.getReplicationSlotDropAttemptsTimeoutUnits(), checkingEvery(100, MILLISECONDS));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureTestDecodingReplicationSlotExists() {
        try (BaseConnection replicationConnection = postgresDatabaseClient.createReplicationConnection()) {
            if (thereIsNoReplicationSlotNamed(slotName)) {
                try {
                    replicationConnection.getReplicationAPI()
                            .createReplicationSlot()
                            .logical()
                            .withSlotName(slotName)
                            .withOutputPlugin("test_decoding")
                            .make();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean thereIsNoReplicationSlotNamed(String slotName) {
        return dbi.withHandle(handle -> {
            int slotCount = handle.createQuery("SELECT COUNT(*) FROM pg_replication_slots WHERE slot_name = :slot_name")
                    .bind("slot_name", slotName)
                    .map(IntegerColumnMapper.PRIMITIVE)
                    .first();

            return slotCount == 0;
        });
    }

    public String getName() {
        return slotName;
    }

}
