package com.ssrn.papers.replicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaperReplicationJobConfiguration {
    private final int jobBatchSize;
    private int databaseUpsertBatchSize;

    @JsonCreator()
    public PaperReplicationJobConfiguration(
            @JsonProperty(value = "jobBatchSize", required = true) int jobBatchSize,
            @JsonProperty(value = "databaseUpsertBatchSize", required = true) int databaseUpsertBatchSize
    ) {
        this.jobBatchSize = jobBatchSize;
        this.databaseUpsertBatchSize = databaseUpsertBatchSize;
    }

    int getJobBatchSize() {
        return jobBatchSize;
    }

    int getDatabaseUpsertBatchSize() {
        return databaseUpsertBatchSize;
    }
}
