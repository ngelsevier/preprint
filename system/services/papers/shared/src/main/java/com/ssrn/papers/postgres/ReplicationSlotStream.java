package com.ssrn.papers.postgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.postgresql.core.BaseConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static com.ssrn.papers.postgres.Interval.checkingEvery;
import static com.ssrn.papers.postgres.RetryingSupplier.tryToSupply;
import static com.ssrn.shared.concurrency.RetryUtils.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ReplicationSlotStream implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationSlotStream.class);
    private static final int SECONDS_TO_WAIT_FOR_ALL_REPLICATION_CONNECTIONT_TO_BE_ESTABLISHED_BEFORE_SHUTTING_DOWN = 10;

    private final String slotName;
    private final int replicationStreamCreationAttemptsTimeout;
    private final TimeUnit replicationStreamCreationAttemptsTimeoutUnit;
    private final int replicationStreamStatusInterval;
    private final TimeUnit replicationStreamStatusIntervalUnits;
    private final PostgresDatabaseClient postgresDatabaseClient;
    private PGReplicationStream pgReplicationStream;
    private BaseConnection replicationConnection;

    public ReplicationSlotStream(String slotName, int replicationStreamCreationAttemptsTimeout, TimeUnit replicationStreamCreationAttemptsTimeoutUnit, int replicationStreamStatusInterval, TimeUnit replicationStreamStatusIntervalUnits, PostgresDatabaseClient postgresDatabaseClient) {
        this.slotName = slotName;
        this.replicationStreamCreationAttemptsTimeout = replicationStreamCreationAttemptsTimeout;
        this.replicationStreamCreationAttemptsTimeoutUnit = replicationStreamCreationAttemptsTimeoutUnit;
        this.replicationStreamStatusInterval = replicationStreamStatusInterval;
        this.replicationStreamStatusIntervalUnits = replicationStreamStatusIntervalUnits;
        this.postgresDatabaseClient = postgresDatabaseClient;
    }

    public Checkpointer getCheckpointer() {
        return new Checkpointer(getPgReplicationStream());
    }

    public ByteBuffer tryAndGetPendingReplicationMessage() {
        try {
            return getPgReplicationStream().readPending();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LogSequenceNumber getLastReceiveLSN() {
        return getPgReplicationStream().getLastReceiveLSN();
    }

    @Override
    public void close() {
        try {
            waitUntil(
                    () -> replicationConnection != null,
                    () -> LOGGER.warn("Timed out waiting for replication connection to be established before proceeding to close it. Waited %s seconds."),
                    SECONDS_TO_WAIT_FOR_ALL_REPLICATION_CONNECTIONT_TO_BE_ESTABLISHED_BEFORE_SHUTTING_DOWN
            );

            if (replicationConnection != null) {
                replicationConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PGReplicationStream getPgReplicationStream() {
        if (pgReplicationStream == null) {
            ChainedLogicalStreamBuilder logicalStreamBuilder = getReplicationConnection().getReplicationAPI()
                    .replicationStream()
                    .logical()
                    .withSlotName(slotName)
                    .withSlotOption("include-xids", true)
                    .withSlotOption("include-timestamp", true)
                    .withSlotOption("force-binary", false)
                    .withStatusInterval(replicationStreamStatusInterval, replicationStreamStatusIntervalUnits);

            pgReplicationStream = tryToSupply
                    (() -> {
                        try {
                            return logicalStreamBuilder.start();
                        } catch (SQLException e) {
                            if (PostgresUtils.databaseObjectInUseCaused(e)) {
                                throw new DatabaseObjectInUseException();
                            }

                            throw new RuntimeException(e);
                        }
                    })
                    .retryingOn(DatabaseObjectInUseException.class)
                    .forNoMoreThan(replicationStreamCreationAttemptsTimeout, replicationStreamCreationAttemptsTimeoutUnit, checkingEvery(100, MILLISECONDS));
        }

        return pgReplicationStream;
    }

    private BaseConnection getReplicationConnection() {
        if (replicationConnection == null) {
            replicationConnection = postgresDatabaseClient.createReplicationConnection();
        }

        return replicationConnection;
    }
}
