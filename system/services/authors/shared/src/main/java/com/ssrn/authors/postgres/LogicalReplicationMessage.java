package com.ssrn.authors.postgres;

import org.postgresql.replication.LogSequenceNumber;

import java.util.Map;

public interface LogicalReplicationMessage {
    String getValueInsertedInto(String columnName);

    Map<String, String> getRow();

    boolean isInsertOrUpdateOnTable(String fullyQualifiedTableName);

    boolean isTransactionCommit();

    LogSequenceNumber getLogSequenceNumber();

    String getContent();
}
