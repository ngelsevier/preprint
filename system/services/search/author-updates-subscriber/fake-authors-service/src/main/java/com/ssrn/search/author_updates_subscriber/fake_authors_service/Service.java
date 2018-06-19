package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static com.amazonaws.SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY;


public class Service extends Application<ServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new Service().run("server");
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) throws Exception {
        KinesisStreamClient kinesisStreamClient = new KinesisStreamClient(getKinesisClientConfiguration());
        MetadataResource metadataResource = new MetadataResource(kinesisStreamClient);
        environment.jersey().register(metadataResource);
        environment.jersey().register(new HealthcheckResource());

        environment.healthChecks().register("kinesis", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                try {
                    kinesisStreamClient.writeRecordToStream("fake-authors-service-healthchecks", "Healthcheck", "Healthcheck");
                    return Result.healthy();
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                }
            }
        });
    }

    KinesisClientConfiguration getKinesisClientConfiguration() {
        System.getProperties().put(DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
        System.getProperties().put(AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");

        return new KinesisClientConfiguration(Boolean.TRUE, "us-east-1", "kinesis", 4567);
    }
}
