package com.ssrn.authors.replicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorReplicationJobConfiguration {
    private final int jobBatchSize;
    private int databaseUpsertBatchSize;

    @JsonCreator()
    public AuthorReplicationJobConfiguration(
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
