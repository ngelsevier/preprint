package com.ssrn.papers.postgres.test_decoding;

import com.ssrn.papers.postgres.LogicalReplicationSlot;
import com.ssrn.papers.postgres.LogicalReplicationSlotSource;
import com.ssrn.papers.postgres.PostgresDatabaseClient;
import com.ssrn.papers.postgres.PostgresReplicationStreamingConfiguration;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.util.StringColumnMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestDecodingLogicalReplicationSlotSource implements LogicalReplicationSlotSource {
    private static final Pattern REPLICATION_SLOT_INDEX_EXTRACTION_PATTERN = Pattern.compile(".+_(\\d+)$");

    private final DBI dbi;
    private final PostgresDatabaseClient postgresDatabaseClient;
    private final PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration;

    public TestDecodingLogicalReplicationSlotSource(DBI dbi, PostgresDatabaseClient postgresDatabaseClient, PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration) {
        this.dbi = dbi;
        this.postgresDatabaseClient = postgresDatabaseClient;
        this.postgresReplicationStreamingConfiguration = postgresReplicationStreamingConfiguration;
    }

    @Override
    public LogicalReplicationSlot getSlotWithIndex(int index) {
        return new TestDecodingLogicalReplicationSlot(nameOfSlotWithIndex(index), index, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient);
    }

    @Override
    public List<LogicalReplicationSlot> getSlots() {
        List<String> slotNames = dbi.withHandle(handle ->
                handle.createQuery(String.format("SELECT slot_name FROM pg_replication_slots WHERE slot_name LIKE '%s_%%'", postgresReplicationStreamingConfiguration.getSlotNamePrefix()))
                        .map(StringColumnMapper.INSTANCE)
                        .list()
        );

        return slotNames.stream()
                .map(TestDecodingLogicalReplicationSlotSource::determineIndexOfSlotNamed)
                .map(slotIndex -> new TestDecodingLogicalReplicationSlot(nameOfSlotWithIndex(slotIndex), slotIndex, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient))
                .collect(Collectors.toList());
    }

    private String nameOfSlotWithIndex(int index) {
        return String.format("%s_%d", postgresReplicationStreamingConfiguration.getSlotNamePrefix(), index);
    }

    private static int determineIndexOfSlotNamed(String slotName) {
        Matcher matcher = REPLICATION_SLOT_INDEX_EXTRACTION_PATTERN.matcher(slotName);

        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new RuntimeException("Unable to parse slot index from name");
        }
    }
}
