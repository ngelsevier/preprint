package com.ssrn.papers.shared.test_support.postgres;

import com.ssrn.papers.domain.Paper;
import org.joda.time.DateTime;
import org.postgresql.util.PSQLState;

import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.ssrn.papers.postgres.Interval.checkingEvery;
import static com.ssrn.papers.postgres.Interval.checkingEvery100Milliseconds;
import static com.ssrn.papers.postgres.RetryingSupplier.tryToSupply;
import static com.ssrn.test.support.utils.SystemCommandUtils.executeSystemCommand;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PostgresDatabase implements AutoCloseable {
    private static final String CANNOT_CONNECT_NOW_PSQL_STATE_CODE = "57P03";
    private static final List<String> POSTGRES_CONNECTION_ERROR_STATES = asList(
            PSQLState.CONNECTION_UNABLE_TO_CONNECT.getState(),
            PSQLState.CONNECTION_FAILURE.getState(),
            CANNOT_CONNECT_NOW_PSQL_STATE_CODE
    );
    private static final String DOCKER_CONTAINER_NAME = "papers_postgres_1";

    private final String eventsEmitterUsername;
    private final String eventsEmitterPassword;
    private final String host;
    private final int port;
    private final String databaseName;
    private Connection connection;
    private String replicatorUsername;
    private String replicatorPassword;
    private String masterUsername;
    private String masterPassword;

    PostgresDatabase(String host, int port, String databaseName, String eventsEmitterUsername, String eventsEmitterPassword, String replicatorUsername, String replicatorPassword, String masterUsername, String masterPassword) {
        this.host = host;
        this.port = port;
        this.eventsEmitterUsername = eventsEmitterUsername;
        this.eventsEmitterPassword = eventsEmitterPassword;
        this.databaseName = databaseName;
        this.replicatorUsername = replicatorUsername;
        this.replicatorPassword = replicatorPassword;
        this.masterUsername = masterUsername;
        this.masterPassword = masterPassword;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getEventsEmitterUsername() {
        return eventsEmitterUsername;
    }

    public String getEventsEmitterPassword() {
        return eventsEmitterPassword;
    }

    public String getReplicatorUsername() {
        return replicatorUsername;
    }

    public String getReplicatorPassword() {
        return replicatorPassword;
    }

    public void ensureStarted() {
        executeSystemCommand(String.format("docker start %s", DOCKER_CONTAINER_NAME));

        tryToSupply(
                () -> {
                    try (Connection connection = createConnection()) {
                        return connection.prepareStatement("SELECT 1").execute();
                    } catch (SQLException e) {
                        if (connectionFailureCaused(e)) {
                            throw new ConnectionFailureException();
                        }

                        throw new RuntimeException(e);
                    }
                })
                .retryingOn(ConnectionFailureException.class)
                .forNoMoreThan(60, SECONDS, checkingEvery100Milliseconds());
    }

    public void ensureStopped() {
        executeSystemCommand(String.format("docker stop %s", DOCKER_CONTAINER_NAME));
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void truncateTables() {
        try {
            truncateTableNamed("paper", getConnection());
            truncateTableNamed("job_checkpoint", getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void dropReplicationSlots() {
        try {
            ResultSet resultSet = getReplicationSlotsIn(databaseName, getConnection());

            while (resultSet.next()) {
                dropReplicationSlotNamed(resultSet.getString("slot_name"), getConnection());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DateTime getLastUpdatedTimeForPaper(Paper paper) {
        try {
            PreparedStatement preparedStatement = getConnection()
                    .prepareStatement("select last_updated from paper where id = ?");
            preparedStatement.setString(1, paper.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new RuntimeException(String.format("Expected to find paper in database with id '%s'", paper.getId()));
            }

            return new DateTime(resultSet.getTimestamp(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void truncateTableNamed(String tableName, Connection connection) throws SQLException {
        connection
                .prepareStatement(String.format("truncate %s", tableName))
                .execute();
    }

    public int replicationSlotCount() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("select count(*) from pg_replication_slots where database = ?");
            preparedStatement.setString(1, databaseName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new RuntimeException("Expected SQL query to return results");
            }

            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultSet getReplicationSlotsIn(String databaseName, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select slot_name from pg_replication_slots where database = ?");
        preparedStatement.setString(1, databaseName);
        return preparedStatement.executeQuery();
    }

    private static void dropReplicationSlotNamed(String slotName, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select pg_drop_replication_slot(?)");
        preparedStatement.setString(1, slotName);

        tryToSupply(
                () -> {
                    try {
                        return preparedStatement.execute();
                    } catch (SQLException e) {
                        if (databaseObjectInUseCaused(e)) {
                            throw new DatabaseObjectInUseException();
                        }

                        throw new RuntimeException(e);
                    }
                })
                .retryingOn(DatabaseObjectInUseException.class)
                .forNoMoreThan(60, SECONDS, checkingEvery100Milliseconds());
    }

    private Connection createConnection() {
        String connectionString = String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);

        Properties props = new Properties();
        props.setProperty("user", masterUsername);
        props.setProperty("password", masterPassword);

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
                .forNoMoreThan(60, SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS));
    }

    private Connection getConnection() {
        if (connection == null) {
            connection = createConnection();
        }

        return connection;
    }

    private static boolean connectionFailureCaused(SQLException e) {
        return POSTGRES_CONNECTION_ERROR_STATES.contains(e.getSQLState());
    }

    private static class ConnectionFailureException extends RuntimeException {


    }

    private static class DatabaseObjectInUseException extends RuntimeException {


    }

    private static boolean databaseObjectInUseCaused(SQLException e) {
        return PSQLState.OBJECT_IN_USE.getState().equals(e.getSQLState());
    }


}
