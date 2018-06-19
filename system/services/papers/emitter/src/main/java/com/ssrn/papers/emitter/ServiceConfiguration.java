package com.ssrn.papers.emitter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;

import java.util.Arrays;
import java.util.Collections;

import static com.ssrn.shared.kms.KmsUtils.usingKmsDecrypt;

public class ServiceConfiguration extends Configuration {

    private DataSourceFactory dataSourceFactory;

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
        String logFilename = "/var/log/emitter/emitter.log";
        fileAppenderFactory.setCurrentLogFilename(logFilename);
        fileAppenderFactory.setMaxFileSize(Size.megabytes(5));
        fileAppenderFactory.setArchivedLogFilenamePattern(logFilename + ".%i");
        fileAppenderFactory.setArchivedFileCount(10);
        loggingFactory.setAppenders(Arrays.asList(new ConsoleAppenderFactory<>(), fileAppenderFactory));
        setLoggingFactory(loggingFactory);

        dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass(org.postgresql.Driver.class.getName());
        dataSourceFactory.setUser("emitter");
        dataSourceFactory.setPassword(Boolean.parseBoolean(System.getenv("SIMULATED_ENVIRONMENT")) ? "emitter" : usingKmsDecrypt(System.getenv("ENCRYPTED_DATABASE_PASSWORD")));
        dataSourceFactory.setUrl("jdbc:postgresql://papers-database.internal-service:5432/papers");
        dataSourceFactory.setMaxWaitForConnection(Duration.milliseconds(500));
        dataSourceFactory.setMinSize(2);
        dataSourceFactory.setInitialSize(2);
    }


    PooledDataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }
}
