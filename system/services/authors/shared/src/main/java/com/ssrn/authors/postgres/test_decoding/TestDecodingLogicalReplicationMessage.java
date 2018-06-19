package com.ssrn.authors.postgres.test_decoding;

import com.ssrn.authors.postgres.LogicalReplicationMessage;
import org.postgresql.replication.LogSequenceNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

class TestDecodingLogicalReplicationMessage implements LogicalReplicationMessage {
    private static final Pattern TABLE_UPSERT_MESSAGE_PATTERN = Pattern.compile("table \\w+\\.\\w+: (?:INSERT|UPDATE):(.+)", Pattern.DOTALL);
    private static final Pattern TABLE_UPSERT_MESSAGE_COLUMN_PATTERN = Pattern.compile(" ([^\\[]+)\\[[^\\[]+\\]:(\\d+|null|'((?:[^']|'')+)')");

    private final String pgContent;
    private final LogSequenceNumber logSequenceNumber;
    private HashMap<String, String> upsertedRow;

    TestDecodingLogicalReplicationMessage(String pgContent, LogSequenceNumber logSequenceNumber) {
        this.pgContent = pgContent;
        this.logSequenceNumber = logSequenceNumber;
    }

    @Override
    public String getValueInsertedInto(String columnName) {
        return getRow().get(columnName);
    }

    @Override
    public Map<String, String> getRow() {
        if (upsertedRow == null) {
            String upsertedRowColumnData = extractUpsertMessageColumnDataStringFrom(pgContent);

            Matcher matcher = TABLE_UPSERT_MESSAGE_COLUMN_PATTERN.matcher(upsertedRowColumnData);
            upsertedRow = new HashMap<>();

            while (matcher.find()) {
                final String columnName = matcher.group(1);
                final String columnValue;

                if ("null".equals(matcher.group(2))) {
                    columnValue = matcher.group(3);
                } else {
                    columnValue = matcher.group(3) == null ? matcher.group(2) : matcher.group(3);
                }

                String parsedColumnValue =
                        (asList("data", "entity").contains(columnName) && columnValue != null) ?
                                columnValue.replaceAll("''", "'") : columnValue;
                upsertedRow.put(columnName, parsedColumnValue);
            }
        }

        return upsertedRow;
    }

    private static String extractUpsertMessageColumnDataStringFrom(String replicationMessage) {
        Matcher tableUpsertMessageMatcher = TABLE_UPSERT_MESSAGE_PATTERN.matcher(replicationMessage);
        tableUpsertMessageMatcher.matches();
        return tableUpsertMessageMatcher.group(1);
    }

    @Override
    public boolean isInsertOrUpdateOnTable(String fullyQualifiedTableName) {
        return Stream.of(
                String.format("table %s: INSERT:", fullyQualifiedTableName),
                String.format("table %s: UPDATE:", fullyQualifiedTableName)
        ).anyMatch(pgContent::startsWith);
    }

    @Override
    public boolean isTransactionCommit() {
        return pgContent.startsWith("COMMIT ");
    }

    @Override
    public LogSequenceNumber getLogSequenceNumber() {
        return logSequenceNumber;
    }

    @Override
    public String getContent() {
        return pgContent;
    }
}
