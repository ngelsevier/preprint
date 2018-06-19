package com.ssrn.search.author_updates_subscriber;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.LoggerConfiguration;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.util.Size;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ServiceConfiguration extends Configuration {
    public ServiceConfiguration() {
        DefaultServerFactory defaultServerFactory = new DefaultServerFactory();
        defaultServerFactory.setApplicationConnectors(Collections.emptyList());
        defaultServerFactory.setMinThreads(1);
        defaultServerFactory.setMaxThreads(2);
        defaultServerFactory.setAdminConnectors(Collections.emptyList());
        defaultServerFactory.setAdminMinThreads(1);
        defaultServerFactory.setAdminMaxThreads(2);
        setServerFactory(defaultServerFactory);

        DefaultLoggingFactory loggingFactory = new DefaultLoggingFactory();
        loggingFactory.setLevel(Level.INFO.toString());

        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        String logFilename = "/var/log/author-updates-subscriber/author-updates-subscriber.log";
        fileAppenderFactory.setCurrentLogFilename(logFilename);
        fileAppenderFactory.setMaxFileSize(Size.megabytes(5));
        fileAppenderFactory.setArchivedLogFilenamePattern(logFilename + ".%i");
        fileAppenderFactory.setArchivedFileCount(10);
        loggingFactory.setAppenders(Arrays.asList(new ConsoleAppenderFactory<>(), fileAppenderFactory));

        setLoggingFactory(loggingFactory);
    }
}
