package com.ssrn.authors.replicator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static com.ssrn.authors.replicator.EncryptedConfigurationUtils.getDecryptedPassword;

public class ServiceConfiguration extends Configuration{
    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();
    private DataSourceFactory dataSourceFactory;

    public ServiceConfiguration() {
        DefaultLoggingFactory loggingFactory = new DefaultLoggingFactory();
        loggingFactory.setLevel(Level.INFO.toString());
        setLoggingFactory(loggingFactory);
        List<AppenderFactory<ILoggingEvent>> appenders = createAppenderFactoriesFor(
                "/var/log/replicator/replicator.log");
        loggingFactory.setAppenders(appenders);

        DefaultServerFactory serverFactory = new DefaultServerFactory();
        LogbackAccessRequestLogFactory requestLogFactory = new LogbackAccessRequestLogFactory();
        requestLogFactory.setAppenders(ImmutableList.copyOf(createAppenderFactoriesFor(
                "/var/log/replicator/access.log")));
        serverFactory.setRequestLogFactory(requestLogFactory);
        serverFactory.setRegisterDefaultExceptionMappers(false);
        setServerFactory(serverFactory);

        getJerseyClient().setTimeout(Duration.seconds(30));

        dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass(org.postgresql.Driver.class.getName());
        dataSourceFactory.setUser("replicator");
        dataSourceFactory.setPassword(getDecryptedPassword("replicator", "ENCRYPTED_DATABASE_PASSWORD"));
        dataSourceFactory.setUrl("jdbc:postgresql://authors-database.internal-service:5432/authors");
        dataSourceFactory.setMaxWaitForConnection(Duration.milliseconds(500));
        dataSourceFactory.setMinSize(2);
        dataSourceFactory.setInitialSize(2);

        jerseyClient.setConnectionTimeout(Duration.seconds(10));
    }

    JerseyClientConfiguration getJerseyClient() {
        return jerseyClient;
    }

    PooledDataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    private <T extends DeferredProcessingAware> List<AppenderFactory<T>> createAppenderFactoriesFor(String logFilename) {
        FileAppenderFactory<T> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(logFilename);
        fileAppenderFactory.setMaxFileSize(Size.megabytes(5));
        fileAppenderFactory.setArchivedLogFilenamePattern(logFilename + ".%i");
        fileAppenderFactory.setArchivedFileCount(10);
        return Arrays.asList(new ConsoleAppenderFactory<>(), fileAppenderFactory);
    }
}
