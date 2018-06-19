package com.ssrn.authors.replicator;

import com.fasterxml.jackson.core.JsonParser;
import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorRepository;
import com.ssrn.authors.domain.Event;
import com.ssrn.authors.postgres.PostgresAuthorRepository;
import com.ssrn.authors.replicator.concurrency.SingleJobRunner;
import com.ssrn.authors.replicator.http_old_platform_author_entities_feed.AuthorsStreamSource;
import com.ssrn.authors.replicator.http_old_platform_author_events_feed.EventsStreamSource;
import com.ssrn.authors.replicator.postgres.PostgresFeedJobCheckpointer;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.client.Client;

import java.util.logging.Level;

import static com.ssrn.authors.replicator.EncryptedConfigurationUtils.getDecryptedPassword;

public class Service extends Application<ServiceConfiguration> {

    private static final Level HTTP_REQUEST_LOG_LEVEL = Level.parse(parseEnvironmentVariableAsString("HTTP_REQUEST_LOG_LEVEL", "INFO"));

    private final String eventsFeedBaseUrl = System.getenv("EVENTS_FEED_BASE_URL");
    private final String eventsFeedUsername = System.getenv("EVENTS_FEED_HTTP_BASIC_AUTH_USERNAME");
    private final String eventsFeedPassword = System.getenv("EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD");
    private static final int ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES = parseEnvironmentVariableAsInt("ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES", 3);
    private static final int EVENTS_FEED_MAX_PAGE_REQUEST_RETRIES = parseEnvironmentVariableAsInt("EVENTS_FEED_MAX_PAGE_REQUEST_RETRIES", 3);

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {

        DBI dbi = new DBIFactory().build(environment, configuration.getDataSourceFactory(), "database");

        AuthorRepository authorRepository = new PostgresAuthorRepository(dbi);

        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(
                new EventsStreamSource(
                        eventsFeedBaseUrl,
                        eventsFeedUsername,
                        getDecryptedPassword(eventsFeedPassword, "EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD"),
                        createHttpClient("Events Feed", configuration, environment),
                        EVENTS_FEED_MAX_PAGE_REQUEST_RETRIES,
                        HTTP_REQUEST_LOG_LEVEL
                ),
                authorRepository,
                new PostgresFeedJobCheckpointer<>(dbi, "Event Feed", Event::getId));

        AuthorReplicator authorReplicator = new AuthorReplicator(
                new AuthorsStreamSource(
                        eventsFeedBaseUrl,
                        eventsFeedUsername,
                        getDecryptedPassword(eventsFeedPassword, "EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD"),
                        createHttpClient("Entity Feed", configuration, environment),
                        ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES,
                        HTTP_REQUEST_LOG_LEVEL
                ), authorRepository, new PostgresFeedJobCheckpointer<>(dbi, "Entity Feed", Author::getId)
        );

        environment.jersey().register(new JobsResource(authorEventsProjector, authorReplicator, new SingleJobRunner(), new SingleJobRunner()));
        environment.jersey().register(new HealthcheckResource());
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
