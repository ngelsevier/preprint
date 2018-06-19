package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.shared.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static com.ssrn.shared.dropwizard.AutoclosingManagedObject.managedObjectForAutocloseable;

public class Service extends Application<ServiceConfiguration> {
    private static final boolean SIMULATED_ENVIRONMENT = Boolean.parseBoolean(System.getenv("SIMULATED_ENVIRONMENT"));
    private static final boolean LOG_INDIVIDUAL_AUTHOR_UPDATES = Boolean.parseBoolean(System.getenv("LOG_INDIVIDUAL_AUTHOR_UPDATES"));
    private static final String ELASTICSEARCH_HOST = "localhost";
    private static final int ELASTICSEARCH_PORT = 9200;
    private static final int DEFAULT_SCROLL_SIZE = 100;
    private static final String KCL_APPLICATION_NAME = System.getenv("KCL_APPLICATION_NAME");
    private static final String KINESIS_STREAM_NAME = System.getenv("KINESIS_STREAM_NAME");

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        KinesisAuthorUpdatesStream authorUpdatesStream = new KinesisAuthorUpdatesStream(
                KCL_APPLICATION_NAME,
                KINESIS_STREAM_NAME,
                SIMULATED_ENVIRONMENT ?
                        new KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration(
                                "us-east-1",
                                "http://kinesis:8000",
                                "https://kinesis:4567",
                                "dummy",
                                "dummy"
                        ) :
                        null,
                LOG_INDIVIDUAL_AUTHOR_UPDATES);

        AuthorUpdatesSubscriber authorUpdatesSubscriber = new AuthorUpdatesSubscriber(
                new ElasticsearchLibrary("papers", "authors", ELASTICSEARCH_HOST, ELASTICSEARCH_PORT, getEnvironmentVariableAsInteger("ELASTICSEARCH_SCROLL_SIZE", DEFAULT_SCROLL_SIZE)),
                new ElasticsearchAuthorRegistry("authors", ELASTICSEARCH_HOST, ELASTICSEARCH_PORT)
        );

        environment.lifecycle().manage(managedObjectForAutocloseable(authorUpdatesStream, authorUpdatesSubscriber::subscribeTo));
    }

    private static int getEnvironmentVariableAsInteger(String name, int defaultValue) {
        String rawValue = System.getenv(name);

        return rawValue == null ?
                defaultValue :
                Integer.parseInt(rawValue);
    }

}

