package com.ssrn.search.papers_consumer;

import com.ssrn.search.shared.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static com.ssrn.shared.dropwizard.AutoclosingManagedObject.managedObjectForAutocloseable;

public class Service extends Application<ServiceConfiguration> {

    private static final boolean SIMULATED_ENVIRONMENT = Boolean.parseBoolean(System.getenv("SIMULATED_ENVIRONMENT"));
    private static final boolean LOG_INDIVIDUAL_PAPERS = Boolean.parseBoolean(System.getenv("LOG_INDIVIDUAL_PAPERS"));
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
        Library library = new ElasticsearchLibrary("papers", "authors", ELASTICSEARCH_HOST, ELASTICSEARCH_PORT, DEFAULT_SCROLL_SIZE);
        AuthorRegistry authorRegistry = new ElasticsearchAuthorRegistry("authors", ELASTICSEARCH_HOST, ELASTICSEARCH_PORT);
        Librarian librarian = new Librarian(library, authorRegistry);

        PapersStream papersStream = new KinesisPapersStream(
                KCL_APPLICATION_NAME,
                KINESIS_STREAM_NAME,
                SIMULATED_ENVIRONMENT ?
                        new KinesisPapersStream.SimulatedEnvironmentConfiguration(
                                "us-east-1",
                                "http://kinesis:8000",
                                "https://kinesis:4567",
                                "dummy",
                                "dummy"
                        ) :
                        null,
                LOG_INDIVIDUAL_PAPERS);

        environment.lifecycle().manage(managedObjectForAutocloseable(papersStream,
                stream -> stream.onPapersReceived(librarian::updatePapers)));
    }

}

