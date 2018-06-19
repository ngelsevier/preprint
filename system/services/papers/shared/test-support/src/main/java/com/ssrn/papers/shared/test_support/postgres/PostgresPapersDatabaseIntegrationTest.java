package com.ssrn.papers.shared.test_support.postgres;

import org.junit.After;
import org.junit.Before;

public abstract class PostgresPapersDatabaseIntegrationTest {

    private PostgresDatabase postgresDatabase;

    @Before()
    public void setUpDatabase() {
        postgresDatabase = new PostgresDatabase(
                "localhost",
                5432,
                "papers_integration_tests",
                "emitter",
                "emitter",
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
        return "papers_test";
    }
}
