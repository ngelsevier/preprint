package com.ssrn.papers.replicator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static com.ssrn.papers.replicator.EncryptedConfigurationUtils.getDecryptedPassword;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class ServiceConfiguration extends Configuration {
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
        dataSourceFactory.setUrl("jdbc:postgresql://papers-database.internal-service:5432/papers");
        dataSourceFactory.setMaxWaitForConnection(Duration.milliseconds(500));
        dataSourceFactory.setMinSize(2);
        dataSourceFactory.setInitialSize(2);

        jerseyClient.setConnectionTimeout(Duration.seconds(5));
    }

    private <T extends DeferredProcessingAware> List<AppenderFactory<T>> createAppenderFactoriesFor(String logFilename) {
        FileAppenderFactory<T> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(logFilename);
        fileAppenderFactory.setMaxFileSize(Size.megabytes(5));
        fileAppenderFactory.setArchivedLogFilenamePattern(logFilename + ".%i");
        fileAppenderFactory.setArchivedFileCount(10);
        return Arrays.asList(new ConsoleAppenderFactory<>(), fileAppenderFactory);
    }

    JerseyClientConfiguration getJerseyClient() {
        return jerseyClient;
    }

    PooledDataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }
}

class UnhandledExceptionMapper extends LoggingExceptionMapper<RuntimeException> {

    private final static Logger LOGGER = LoggerFactory.getLogger(UnhandledExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof WebApplicationException) {
            return super.toResponse(exception);
        }
        LOGGER.error("Unhandled exception was encountered ", exception);
        return Response.status(INTERNAL_SERVER_ERROR).build();
    }
}