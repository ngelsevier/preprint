package com.ssrn.papers.postgres;

import com.ssrn.papers.postgres.test_decoding.TestDecodingLogicalReplicationSlot;
import com.ssrn.papers.shared.test_support.postgres.PostgresPapersDatabaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.sql.SQLException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class TestDecodingLogicalReplicationSlotTest extends PostgresPapersDatabaseIntegrationTest {

    private static final int ANY_INDEX = 0;
    private PostgresReplicationStreamingConfiguration postgresReplicationStreamingConfiguration;
    private PostgresDatabaseClientConfiguration configuration;
    private DBI dbi;

    @Before
    public void before() {
        configuration = new PostgresDatabaseClientConfiguration(
                postgresDatabase().getHost(),
                postgresDatabase().getPort(),
                postgresDatabase().getDatabaseName(),
                postgresDatabase().getEventsEmitterUsername(),
                postgresDatabase().getEventsEmitterPassword(),
                2, SECONDS,
                30, SECONDS
        );

        postgresReplicationStreamingConfiguration = new PostgresReplicationStreamingConfiguration(getTestSlotNamePrefix(), 30, SECONDS, 20, SECONDS, 1, SECONDS);

        dbi = new DBI(
                String.format("jdbc:postgresql://%s:%d/%s", postgresDatabase().getHost(), postgresDatabase().getPort(), postgresDatabase().getDatabaseName()),
                postgresDatabase().getReplicatorUsername(),
                postgresDatabase().getReplicatorPassword()
        );
    }

    @Test
    public void shouldBeAbleToCreateStreamForExistingReplicationSlot() throws SQLException {
        // Given
        PostgresDatabaseClient postgresDatabaseClient = new PostgresDatabaseClient(configuration);

        TestDecodingLogicalReplicationSlot firstSlot = new TestDecodingLogicalReplicationSlot("test", ANY_INDEX, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient);

        try (ReplicationSlotStream ignored = firstSlot.createStream()) {
        }

        TestDecodingLogicalReplicationSlot secondSlot = new TestDecodingLogicalReplicationSlot("test", ANY_INDEX, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient);

        // When
        try (ReplicationSlotStream ignored = secondSlot.createStream()) {
        }

        // Then no exception
    }

    @Test
    public void shouldDropReplicationSlot() throws SQLException {
        // Given
        PostgresDatabaseClient postgresDatabaseClient = new PostgresDatabaseClient(configuration);

        TestDecodingLogicalReplicationSlot firstSlot = new TestDecodingLogicalReplicationSlot("test", ANY_INDEX, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient);

        try (ReplicationSlotStream ignored = firstSlot.createStream()) {
        }

        TestDecodingLogicalReplicationSlot secondSlot = new TestDecodingLogicalReplicationSlot("test", ANY_INDEX, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient);
        assertThat(postgresDatabase().replicationSlotCount(), is(equalTo(1)));

        // When
        secondSlot.drop();

        // Then
        assertThat(postgresDatabase().replicationSlotCount(), is(equalTo(ANY_INDEX)));
    }

    @Test()
    public void shouldNotSuppressExceptionsThrownWhenCreatingReplicationSlot() throws SQLException, InterruptedException {
        // Given
        PostgresDatabaseClient postgresDatabaseClient = new PostgresDatabaseClient(configuration);

        try {
            TestDecodingLogicalReplicationSlot replicationSlot = new TestDecodingLogicalReplicationSlot(String.format("%s_%d", "invalid-slot-name", ANY_INDEX), ANY_INDEX, postgresReplicationStreamingConfiguration, dbi, postgresDatabaseClient);

            // When
            try (ReplicationSlotStream ignored = replicationSlot.createStream()) {
            }

            fail("Expected exception to be thrown");
        } catch (RuntimeException e) {
            assertThat(e.getCause(), is(instanceOf(SQLException.class)));
        }
    }


}
