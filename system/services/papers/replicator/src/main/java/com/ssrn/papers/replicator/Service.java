package com.ssrn.papers.replicator;

import com.fasterxml.jackson.core.JsonParser;
import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.PaperRepository;
import com.ssrn.papers.postgres.PostgresPaperRepository;
import com.ssrn.papers.replicator.concurrency.SingleJobRunner;
import com.ssrn.papers.replicator.http_old_platform_paper_entities_feed.PapersStreamSource;
import com.ssrn.papers.replicator.http_old_platform_paper_events_feed.EventsStreamSource;
import com.ssrn.papers.replicator.postgres.PostgresFeedJobCheckpointer;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.client.Client;

import java.util.logging.Level;

import static com.ssrn.papers.replicator.EncryptedConfigurationUtils.getDecryptedPassword;

public class Service extends Application<ServiceConfiguration> {

    private static final Level HTTP_REQUEST_LOG_LEVEL = Level.parse(parseEnvironmentVariableAsString("HTTP_REQUEST_LOG_LEVEL", "INFO"));
    private static final int ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES = parseEnvironmentVariableAsInt("ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES", 3);
    private static final int EVENTS_FEED_MAX_PAGE_REQUEST_RETRIES = parseEnvironmentVariableAsInt("EVENTS_FEED_MAX_PAGE_REQUEST_RETRIES", 3);

    private final String eventsFeedBaseUrl = System.getenv("EVENTS_FEED_BASE_URL");

    private final String eventsFeedUsername = System.getenv("EVENTS_FEED_HTTP_BASIC_AUTH_USERNAME");
    private final String eventsFeedPassword = System.getenv("EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD");

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public String getName() {
        return "Replicator";
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) throws Exception {

        DBI dbi = new DBIFactory().build(environment, configuration.getDataSourceFactory(), "database");

        PaperRepository paperRepository = new PostgresPaperRepository(dbi);

        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(
                new EventsStreamSource(
                        eventsFeedBaseUrl,
                        eventsFeedUsername,
                        getDecryptedPassword(eventsFeedPassword, "EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD"),
                        createHttpClient("Events Feed", configuration, environment),
                        EVENTS_FEED_MAX_PAGE_REQUEST_RETRIES,
                        HTTP_REQUEST_LOG_LEVEL
                ),
                paperRepository,
                new PostgresFeedJobCheckpointer<>(dbi, "Event Feed", Paper.Event::getId));

        PaperReplicator paperReplicator = new PaperReplicator(new PapersStreamSource(
                eventsFeedBaseUrl,
                eventsFeedUsername,
                getDecryptedPassword(eventsFeedPassword, "EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD"),
                createHttpClient("Entity Feed", configuration, environment),
                ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES,
                HTTP_REQUEST_LOG_LEVEL
        ),
                paperRepository,
                new PostgresFeedJobCheckpointer<>(dbi, "Entity Feed", Paper::getId)
        );

        environment.jersey().register(new JobsResource(paperEventsReplicator, paperReplicator, new SingleJobRunner(), new SingleJobRunner()));
        environment.jersey().register(new HealthcheckResource());
        environment.jersey().register(new UnhandledExceptionMapper());
    }

    private Client createHttpClient(String name, ServiceConfiguration configuration, Environment environment) {
        return new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClient())
                .build(name)
                .register(new JacksonMessageBodyProvider(environment.getObjectMapper().configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)));
    }

    private static int parseEnvironmentVariableAsInt(String name, int defaultValue) {
        String rawValue = System.getenv(name);
        return rawValue == null ? defaultValue : Integer.parseInt(rawValue);
    }

    private static String parseEnvironmentVariableAsString(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
