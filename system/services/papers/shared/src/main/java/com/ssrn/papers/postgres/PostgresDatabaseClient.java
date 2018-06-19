package com.ssrn.papers.postgres;

import org.postgresql.PGProperty;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PSQLState;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.ssrn.papers.postgres.Interval.checkingEvery;
import static com.ssrn.papers.postgres.RetryingSupplier.tryToSupply;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PostgresDatabaseClient {

    private final String connectionString;
    private final PostgresDatabaseClientConfiguration postgresDatabaseClientConfiguration;
    private final TimeUnit connectionTimeoutUnit;
    private final int connectionTimeout;

    PostgresDatabaseClient(PostgresDatabaseClientConfiguration postgresDatabaseClientConfiguration) {
        this.postgresDatabaseClientConfiguration = postgresDatabaseClientConfiguration;
        connectionString = String.format("jdbc:postgresql://%s:%d/%s", postgresDatabaseClientConfiguration.getHost(), postgresDatabaseClientConfiguration.getPort(), this.postgresDatabaseClientConfiguration.getDatabaseName());
        connectionTimeout = postgresDatabaseClientConfiguration.getConnectionTimeout();
        connectionTimeoutUnit = postgresDatabaseClientConfiguration.getConnectionTimeoutUnit();
    }

    public BaseConnection createReplicationConnection() {
        Properties properties = new Properties();
        PGProperty.USER.set(properties, postgresDatabaseClientConfiguration.getUsername());
        PGProperty.PASSWORD.set(properties, postgresDatabaseClientConfiguration.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(properties, "9.6");
        PGProperty.REPLICATION.set(properties, "database");
        PGProperty.PREFER_QUERY_MODE.set(properties, "simple");
        PGProperty.CONNECT_TIMEOUT.set(properties, (int) connectionTimeoutUnit.toSeconds(connectionTimeout));

        Connection replicationConnection = tryToSupply(
                () -> {
                    try {
                        return DriverManager.getConnection(connectionString, properties);
                    } catch (SQLException e) {
                        if (connectionFailureCaused(e)) {
                            throw new ConnectionFailureException();
                        } else if (tooManyConnectionsCaused(e)) {
                            throw new TooManyConnectionsException();
                        }

                        throw new RuntimeException(e);
                    }
                })
                .retryingOn(ConnectionFailureException.class, TooManyConnectionsException.class)
                .forNoMoreThan(postgresDatabaseClientConfiguration.getConnectionAttemptsTimeout(), postgresDatabaseClientConfiguration.getConnectionAttemptsTimeoutUnit(), checkingEvery(100, MILLISECONDS));

        try {
            return replicationConnection.unwrap(BaseConnection.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean connectionFailureCaused(SQLException e) {
        return PSQLState.CONNECTION_UNABLE_TO_CONNECT.getState().equals(e.getSQLState());
    }

    private static boolean tooManyConnectionsCaused(SQLException e) {
        return "53300".equals(e.getSQLState());
    }

    private static class ConnectionFailureException extends RuntimeException {
    }

    private static class TooManyConnectionsException extends RuntimeException {
    }
}
