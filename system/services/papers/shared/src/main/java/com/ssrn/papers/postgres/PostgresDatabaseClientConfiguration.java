package com.ssrn.papers.postgres;

import java.util.concurrent.TimeUnit;

public class PostgresDatabaseClientConfiguration {
    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;
    private final int connectionAttemptsTimeout;
    private final TimeUnit connectionAttemptsTimeoutUnit;
    private final TimeUnit connectionTimeoutUnit;
    private final int connectionTimeout;

    public PostgresDatabaseClientConfiguration(String host, int port, String databaseName, String username, String password, int connectionTimeout, TimeUnit connectionTimeoutUnit, int connectionAttemptsTimeout, TimeUnit connectionAttemptsTimeoutUnit) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.connectionAttemptsTimeout = connectionAttemptsTimeout;
        this.connectionAttemptsTimeoutUnit = connectionAttemptsTimeoutUnit;
        this.connectionTimeout = connectionTimeout;
        this.connectionTimeoutUnit = connectionTimeoutUnit;
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    String getDatabaseName() {
        return databaseName;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    int getConnectionTimeout() {
        return connectionTimeout;
    }

    TimeUnit getConnectionTimeoutUnit() {
        return connectionTimeoutUnit;
    }

    int getConnectionAttemptsTimeout() {
        return connectionAttemptsTimeout;
    }

    TimeUnit getConnectionAttemptsTimeoutUnit() {
        return connectionAttemptsTimeoutUnit;
    }
}
