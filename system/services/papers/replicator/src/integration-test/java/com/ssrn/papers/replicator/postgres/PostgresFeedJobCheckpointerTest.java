package com.ssrn.papers.replicator.postgres;

import com.ssrn.papers.shared.test_support.postgres.PostgresPapersDatabaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class PostgresFeedJobCheckpointerTest extends PostgresPapersDatabaseIntegrationTest {
    private DBI dbi;

    @Before
    public void before() {
        dbi = new DBI(
                String.format("jdbc:postgresql://%s:%d/%s", postgresDatabase().getHost(), postgresDatabase().getPort(), postgresDatabase().getDatabaseName()),
                postgresDatabase().getReplicatorUsername(),
                postgresDatabase().getReplicatorPassword()
        );
    }

    @Test
    public void shouldRetrieveLastCheckpointSavedToDatabase() {
        // Given
        PostgresFeedJobCheckpointer<String> firstCheckpointer = createCheckpointerThatParsesFeedItemsAsIntegers(dbi);
        PostgresFeedJobCheckpointer<String> secondCheckpointer = createCheckpointerThatParsesFeedItemsAsIntegers(dbi);

        firstCheckpointer.checkpoint("a");

        firstCheckpointer.checkpoint("b");

        // When
        String retrievedCheckpoint = secondCheckpointer.getLastCheckpoint().get();

        // Then
        assertThat(retrievedCheckpoint, is(equalTo("B")));
    }

    @Test
    public void shouldRememberLastCheckpointInMemoryWhenCheckpointing() {
        // Given
        PostgresFeedJobCheckpointer<String> checkpointer = createCheckpointerThatParsesFeedItemsAsIntegers(dbi);

        checkpointer.checkpoint("a");

        postgresDatabase().truncateTables();

        // When
        String retrievedCheckpoint = checkpointer.getLastCheckpoint().get();

        // Then
        assertThat(retrievedCheckpoint, is(equalTo("A")));
    }

    @Test
    public void shouldRememberLastCheckpointInMemoryAfterRetrievingFromDatabase() {
        // Given
        PostgresFeedJobCheckpointer<String> firstCheckpointer = createCheckpointerThatParsesFeedItemsAsIntegers(dbi);
        PostgresFeedJobCheckpointer<String> secondCheckpointer = createCheckpointerThatParsesFeedItemsAsIntegers(dbi);

        firstCheckpointer.checkpoint("a");
        secondCheckpointer.getLastCheckpoint();

        // When
        postgresDatabase().truncateTables();
        String retrievedCheckpoint = secondCheckpointer.getLastCheckpoint().get();

        // Then
        assertThat(retrievedCheckpoint, is(equalTo("A")));
    }

    @Test
    public void shouldIndicateNoCheckpointWhenNoneHasBeenSavedToDatabase() {
        // Given
        PostgresFeedJobCheckpointer<String> jobCheckpointer = createCheckpointerThatParsesFeedItemsAsIntegers(dbi);

        // When
        Optional<String> retrievedCheckpoint = jobCheckpointer.getLastCheckpoint();

        // Then
        assertThat(retrievedCheckpoint.isPresent(), is(false));
    }

    private static PostgresFeedJobCheckpointer<String> createCheckpointerThatParsesFeedItemsAsIntegers(DBI dbi) {
        return new PostgresFeedJobCheckpointer<>(dbi, "Test", String::toUpperCase);
    }
}

