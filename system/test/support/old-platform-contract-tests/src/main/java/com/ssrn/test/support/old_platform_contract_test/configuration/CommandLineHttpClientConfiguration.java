package com.ssrn.test.support.old_platform_contract_test.configuration;

import com.ssrn.test.support.standalone_test_runner.configuration.ConfigurationBase;
import com.ssrn.test.support.http.HttpClientConfiguration;

import java.util.logging.Level;

public class CommandLineHttpClientConfiguration extends ConfigurationBase implements HttpClientConfiguration {

    private static final String CONNECTION_TIMEOUT_MILLISECONDS = "connection-timeout-millis";
    private static final String READ_TIMEOUT_MILLIS = "read-timeout-millis";
    private static final String MAX_ENTITY_BYTES_TO_LOG = "max-entity-bytes-to-log";
    private static final String LOG_LEVEL = "log-level";
    private static final String LOG_ENTITY = "log-entity";

    public CommandLineHttpClientConfiguration(String commandLineArgumentPrefix) {
        super(commandLineArgumentPrefix);

        addParameter(LOG_LEVEL, Level::parse, "OFF", "LEVEL", "log http client requests and responses to standard out");
        addParameter(LOG_ENTITY, "false", "log http client request and response entity bodies to standard out - only takes effect when http client logging is on");
        addParameter(MAX_ENTITY_BYTES_TO_LOG, Integer::parseInt, "8192", "BYTES", "maximum number of bytes that will be logged for a given request or response entity body - only takes effect when http client entity body logging is on");
        addParameter(READ_TIMEOUT_MILLIS, Integer::parseInt, "30000", "MILLISECONDS", "maximum number of milliseconds for http client to wait between receiving bytes");
        addParameter(CONNECTION_TIMEOUT_MILLISECONDS, Integer::parseInt, "30000", "MILLISECONDS", "maximum number of milliseconds for http client to wait for tcp connection to be established");
    }

    @Override
    public int connectionTimeoutMillisseconds() {
        return getValueOf(CONNECTION_TIMEOUT_MILLISECONDS);
    }

    @Override
    public int readTimeoutMilliseconds() {
        return getValueOf(READ_TIMEOUT_MILLIS);
    }

    @Override
    public Level logLevel() {
        return getValueOf(LOG_LEVEL);
    }

    @Override
    public Boolean logEntity() {
        return getValueOf(LOG_ENTITY);
    }

    @Override
    public int maxEntityBytesToLog() {
        return getValueOf(MAX_ENTITY_BYTES_TO_LOG);
    }

}
