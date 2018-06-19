package com.ssrn.authors.replicator.functional_tests;

import org.postgresql.util.PSQLState;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.ssrn.authors.postgres.Interval.checkingEvery;
import static com.ssrn.authors.postgres.RetryingSupplier.tryToSupply;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PostgresDatabase implements AutoCloseable {
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final String databaseName;
    private final Connection connection;

    PostgresDatabase(String host, int port, String username, String password, String databaseName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.connection = createConnection();
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String getLastEntityFeedCheckpoint() {
        try {
            ResultSet resultSet = connection.prepareStatement("select checkpoint from job_checkpoint where job = 'Entity Feed'").executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            return resultSet.getString(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    boolean hasAuthorWithId(String id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select id from author where id = ?");
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static boolean connectionFailureCaused(SQLException e) {
        return PSQLState.CONNECTION_UNABLE_TO_CONNECT.getState().equals(e.getSQLState());
    }

    private Connection createConnection() {
        String connectionString = String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);

        return tryToSupply
                (() -> {
                    try {
                        return DriverManager.getConnection(connectionString, props);
                    } catch (SQLException e) {
                        if (connectionFailureCaused(e)) {
                            throw new ConnectionFailureException();
                        }

                        throw new RuntimeException(e);
                    }
                })
                .retryingOn(ConnectionFailureException.class)
                .forNoMoreThan(30, SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS));
    }

    public String getNameOfAuthorWithId(String id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select entity->>'name' from author where id = ?");
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getString(1) : null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ConnectionFailureException extends RuntimeException {

    }

}
