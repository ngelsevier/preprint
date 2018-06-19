package com.ssrn.authors.postgres;

import java.util.concurrent.TimeUnit;

public class PostgresReplicationStreamingConfiguration {
    private final String slotNamePrefix;
    private final int replicationStreamCreationAttemptsTimeout;
    private final TimeUnit replicationStreamCreationAttemptsTimeoutUnit;
    private final TimeUnit replicationSlotDropAttemptsTimeoutUnits;
    private final int replicationSlotDropAttemptsTimeout;
    private final int replicationStreamStatusInterval;
    private final TimeUnit replicationStreamStatusIntervalUnits;

    public PostgresReplicationStreamingConfiguration(String slotNamePrefix, int replicationStreamCreationAttemptsTimeout, TimeUnit replicationStreamCreationAttemptsTimeoutUnit, int replicationSlotDropAttemptsTimeout, TimeUnit replicationSlotDropAttemptsTimeoutUnits, int replicationStreamStatusInterval, TimeUnit replicationStreamStatusIntervalUnits) {
        this.slotNamePrefix = slotNamePrefix;
        this.replicationStreamCreationAttemptsTimeout = replicationStreamCreationAttemptsTimeout;
        this.replicationStreamCreationAttemptsTimeoutUnit = replicationStreamCreationAttemptsTimeoutUnit;
        this.replicationSlotDropAttemptsTimeoutUnits = replicationSlotDropAttemptsTimeoutUnits;
        this.replicationSlotDropAttemptsTimeout = replicationSlotDropAttemptsTimeout;
        this.replicationStreamStatusInterval = replicationStreamStatusInterval;
        this.replicationStreamStatusIntervalUnits = replicationStreamStatusIntervalUnits;
    }

    public int getReplicationStreamStatusInterval() {
        return replicationStreamStatusInterval;
    }

    public TimeUnit getReplicationStreamStatusIntervalUnits() {
        return replicationStreamStatusIntervalUnits;
    }

    public TimeUnit getReplicationSlotDropAttemptsTimeoutUnits() {
        return replicationSlotDropAttemptsTimeoutUnits;
    }

    public int getReplicationSlotDropAttemptsTimeout() {
        return replicationSlotDropAttemptsTimeout;
    }

    public String getSlotNamePrefix() {
        return slotNamePrefix;
    }

    public int getReplicationStreamCreationAttemptsTimeout() {
        return replicationStreamCreationAttemptsTimeout;
    }

    public TimeUnit getReplicationStreamCreationAttemptsTimeoutUnit() {
        return replicationStreamCreationAttemptsTimeoutUnit;
    }
}
