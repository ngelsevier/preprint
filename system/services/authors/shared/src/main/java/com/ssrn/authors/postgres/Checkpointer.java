package com.ssrn.authors.postgres;

import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import java.sql.SQLException;

public class Checkpointer {
    private final PGReplicationStream pgReplicationStream;

    Checkpointer(PGReplicationStream pgReplicationStream) {
        this.pgReplicationStream = pgReplicationStream;
    }

    void checkpoint(LogSequenceNumber logSequenceNumber) {
        pgReplicationStream.setAppliedLSN(logSequenceNumber);
        pgReplicationStream.setFlushedLSN(logSequenceNumber);
        try {
            pgReplicationStream.forceUpdateStatus();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
