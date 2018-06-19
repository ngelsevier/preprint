package com.ssrn.search.author_updates_subscriber;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.ssrn.search.amazon.NoopAmazonCloudWatch;
import com.ssrn.search.domain.AuthorUpdate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.amazonaws.SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY;

class KinesisAuthorUpdatesStream implements AuthorUpdatesStream {

    private final Worker.Builder workerBuilder;
    private boolean logIndividualAuthorUpdates;
    private Worker worker;

    KinesisAuthorUpdatesStream(String kclApplicationName, String kinesisStreamName, SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration, boolean logIndividualAuthorUpdates) {
        this.logIndividualAuthorUpdates = logIndividualAuthorUpdates;
        AWSCredentialsProvider credentialsProvider = createCredentialsProvider(simulatedEnvironmentConfiguration);

        KinesisClientLibConfiguration kinesisClientLibConfiguration = new KinesisClientLibConfiguration(
                kclApplicationName,
                kinesisStreamName,
                credentialsProvider,
                getWorkerId()
        ).withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON);

        workerBuilder = new Worker.Builder().config(kinesisClientLibConfiguration);

        if (simulatedEnvironmentConfiguration != null) {
            System.getProperties().put(DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
            System.getProperties().put(AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");

            workerBuilder
                    .dynamoDBClient(AmazonDynamoDBClientBuilder.standard()
                            .withCredentials(credentialsProvider)
                            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                                    simulatedEnvironmentConfiguration.getDynamoDbEndpoint(),
                                    simulatedEnvironmentConfiguration.getAwsRegion())).build()
                    )
                    .kinesisClient(AmazonKinesisClientBuilder.standard()
                            .withCredentials(credentialsProvider)
                            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                                    simulatedEnvironmentConfiguration.getKinesisEndpoint(),
                                    simulatedEnvironmentConfiguration.getAwsRegion())).build()
                    )
                    .cloudWatchClient(new NoopAmazonCloudWatch());
        }
    }

    private static AWSCredentialsProvider createCredentialsProvider(SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration) {
        if (simulatedEnvironmentConfiguration == null) {
            return DefaultAWSCredentialsProviderChain.getInstance();
        }

        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                        simulatedEnvironmentConfiguration.getAwsAccessKey(),
                        simulatedEnvironmentConfiguration.getAwsSecretKey()
                )
        );
    }

    @Override
    public void close() throws Exception {
        if (worker != null) {
            worker.requestShutdown().get(30, TimeUnit.SECONDS);
        }
    }

    public String getWorkerId() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAuthorUpdatesReceived(Consumer<List<AuthorUpdate>> authorUpdatesConsumer) {
        worker = workerBuilder.recordProcessorFactory(() -> new AuthorUpdatesRecordProcessor(authorUpdatesConsumer, this.logIndividualAuthorUpdates)).build();
        worker.run();
    }

    public static class SimulatedEnvironmentConfiguration {
        private final String awsRegion;
        private final String dynamoDbEndpoint;
        private final String kinesisEndpoint;
        private final String awsAccessKey;
        private final String awsSecretKey;

        public SimulatedEnvironmentConfiguration(String awsRegion, String dynamoDbEndpoint, String kinesisEndpoint, String awsAccessKey, String awsSecretKey) {
            this.awsRegion = awsRegion;
            this.dynamoDbEndpoint = dynamoDbEndpoint;
            this.kinesisEndpoint = kinesisEndpoint;
            this.awsAccessKey = awsAccessKey;
            this.awsSecretKey = awsSecretKey;
        }

        public String getAwsRegion() {
            return awsRegion;
        }

        public String getDynamoDbEndpoint() {
            return dynamoDbEndpoint;
        }

        public String getKinesisEndpoint() {
            return kinesisEndpoint;
        }

        public String getAwsAccessKey() {
            return awsAccessKey;
        }

        public String getAwsSecretKey() {
            return awsSecretKey;
        }
    }
}
