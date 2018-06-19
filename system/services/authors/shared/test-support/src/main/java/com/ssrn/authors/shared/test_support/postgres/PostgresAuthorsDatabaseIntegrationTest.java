package com.ssrn.authors.shared.test_support.postgres;

import org.junit.After;
import org.junit.Before;

public abstract class PostgresAuthorsDatabaseIntegrationTest {

    private PostgresDatabase postgresDatabase;

    @Before()
    public void setUpDatabase() {
        postgresDatabase = new PostgresDatabase(
                "localhost",
                5432,
                "authors_integration_tests",
                "publisher",
                "publisher",
                "replicator",
                "replicator",
                "postgres",
                "postgres");

        postgresDatabase.ensureStarted();
        postgresDatabase.dropReplicationSlots();
        postgresDatabase.truncateTables();
    }

    @After
    public void after() {
        postgresDatabase.close();
    }

    protected PostgresDatabase postgresDatabase() {
        return postgresDatabase;
    }

    protected String getTestSlotNamePrefix() {
        return "authors_test";
    }
}
