package com.ssrn.logging.fake_logging_client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.server.DefaultServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class ServiceConfiguration extends Configuration {
    public ServiceConfiguration() {
        DefaultLoggingFactory loggingFactory = new DefaultLoggingFactory();
        loggingFactory.setLevel(Level.INFO.toString());
        setLoggingFactory(loggingFactory);
        List<AppenderFactory<ILoggingEvent>> appenders = createAppenderFactoriesFor(
                "/var/log/service/service.log");
        loggingFactory.setAppenders(appenders);

        DefaultServerFactory serverFactory = new DefaultServerFactory();
        LogbackAccessRequestLogFactory requestLogFactory = new LogbackAccessRequestLogFactory();
        requestLogFactory.setAppenders(ImmutableList.copyOf(createAppenderFactoriesFor(
                "/var/log/service/access.log")));
        serverFactory.setRequestLogFactory(requestLogFactory);
        serverFactory.setRegisterDefaultExceptionMappers(false);
        setServerFactory(serverFactory);

    }

    private <T extends DeferredProcessingAware> List<AppenderFactory<T>> createAppenderFactoriesFor(String logFilename) {
        FileAppenderFactory<T> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(logFilename);
        fileAppenderFactory.setArchive(false);
        return Arrays.asList(new ConsoleAppenderFactory<>(), fileAppenderFactory);
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
