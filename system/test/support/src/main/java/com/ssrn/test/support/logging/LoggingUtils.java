package com.ssrn.test.support.logging;

import ch.qos.logback.classic.Logger;

public class LoggingUtils {
    public static Logger getLogbackLoggerNamed(String loggerName) {
        return (Logger) org.slf4j.LoggerFactory.getLogger(loggerName);
    }
}
