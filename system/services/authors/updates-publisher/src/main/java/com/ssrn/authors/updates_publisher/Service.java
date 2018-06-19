package com.ssrn.authors.updates_publisher;

import com.ssrn.authors.domain.AuthorUpdate;
import com.ssrn.authors.postgres.PostgresAuthorRepository;
import com.ssrn.authors.postgres.PostgresDatabaseClientConfiguration;
import com.ssrn.authors.postgres.PostgresReplicationStreamingConfiguration;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;

import static com.ssrn.shared.dropwizard.AutoclosingManagedObject.managedObjectForAutocloseable;
import static com.ssrn.shared.kms.KmsUtils.usingKmsDecrypt;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Service extends Application<ServiceConfiguration> {

    private static final boolean SIMULATED_ENVIRONMENT = Boolean.parseBoolean(System.getenv("SIMULATED_ENVIRONMENT"));
    private static final boolean LOG_INDIVIDUAL_AUTHORS = Boolean.parseBoolean(System.getenv("LOG_INDIVIDUAL_AUTHORS"));
    private static final int MAX_CONCURRENT_EMISSIONS = parseEnvironmentVariableAsInteger("MAX_CONCURRENT_EMISSIONS", 1);
    private static final int SECONDS_BETWEEN_CHECKING_WORKER_THREADS_FOR_EXCEPTIONS = 10;
    private static final String KINESIS_STREAM_NAME = System.getenv("KINESIS_STREAM_NAME");

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        PostgresAuthorRepository postgresAuthorRepository = new PostgresAuthorRepository(
                new PostgresDatabaseClientConfiguration(
                        "authors-database.internal-service",
                        5432,
                        "authors",
                        "publisher",
                        SIMULATED_ENVIRONMENT ? "publisher" : usingKmsDecrypt(System.getenv("ENCRYPTED_DATABASE_PASSWORD")),
                        10, SECONDS,
                        30, SECONDS),
                new PostgresReplicationStreamingConfiguration(
                        "publisher",
                        30, SECONDS,
                        20, SECONDS,
                        10, SECONDS
                ),
                new DBIFactory().build(environment, configuration.getDataSourceFactory(), "database"),
                MAX_CONCURRENT_EMISSIONS,
                SECONDS_BETWEEN_CHECKING_WORKER_THREADS_FOR_EXCEPTIONS
        );

        AuthorUpdatesStream authorUpdatesStream =
                new KinesisAuthorUpdatesStream(
                        SIMULATED_ENVIRONMENT ?
                                new KinesisAuthorUpdatesStream.SimulatedEnvironmentConfiguration(
                                        "us-east-1",
                                        "https://kinesis:4567",
                                        "dummy",
                                        "dummy"
                                ) :
                                null,
                        KINESIS_STREAM_NAME,
                        LOG_INDIVIDUAL_AUTHORS,
                        MAX_CONCURRENT_EMISSIONS * 10000,
                        10, SECONDS);

        environment.lifecycle().manage(managedObjectForAutocloseable(postgresAuthorRepository,
                repository -> repository.onAuthorUpdated(author -> {
                            AuthorUpdate authorUpdate = new AuthorUpdate(author.getId(), author);
                            authorUpdatesStream.publish(authorUpdate);
                        }
                )));
    }

    private static int parseEnvironmentVariableAsInteger(String name, int defaultValue) {
        String rawValue = System.getenv(name);
        return rawValue == null ? defaultValue : Integer.parseInt(rawValue);
    }
}
