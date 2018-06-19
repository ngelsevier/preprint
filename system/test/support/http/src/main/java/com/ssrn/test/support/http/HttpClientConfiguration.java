package com.ssrn.test.support.http;

public interface HttpClientConfiguration {
    int connectionTimeoutMillisseconds();

    int readTimeoutMilliseconds();

    java.util.logging.Level logLevel();

    Boolean logEntity();

    int maxEntityBytesToLog();
}
