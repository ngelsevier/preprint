package com.ssrn.authors.updates_publisher;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ssrn.authors.domain.AuthorUpdate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.amazonaws.SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY;

public class KinesisAuthorUpdatesStream implements AuthorUpdatesStream {

    private final static Logger LOGGER = LoggerFactory.getLogger(KinesisAuthorUpdatesStream.class.getName());

    private final LoadingCache<String, Optional<String>> perAuthorUpdateSequenceNumbers;

    private final String kinesisStreamName;
    private final AmazonKinesis amazonKinesisClient;
    private boolean logIndividualAuthorUpdates;

    public KinesisAuthorUpdatesStream(SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration, String kinesisStreamName, boolean logIndividualAuthorUpdates, int maximumRecordSequenceNumbersToCache, int sequenceNumberCacheTimeout, TimeUnit sequenceNumberCacheTimeoutUnit) {
        this.kinesisStreamName = kinesisStreamName;
        this.logIndividualAuthorUpdates = logIndividualAuthorUpdates;
        AmazonKinesisClientBuilder amazonKinesisClientBuilder = AmazonKinesisClientBuilder.standard();
        amazonKinesisClientBuilder.setCredentials(createCredentialsProvider(simulatedEnvironmentConfiguration));

        if (simulatedEnvironmentConfiguration != null) {
            System.getProperties().put(DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
            System.getProperties().put(AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");

            amazonKinesisClientBuilder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    simulatedEnvironmentConfiguration.getKinesisEndpoint(),
                    simulatedEnvironmentConfiguration.getAwsRegion()));
        }
        amazonKinesisClient = amazonKinesisClientBuilder.build();

        perAuthorUpdateSequenceNumbers = CacheBuilder.newBuilder()
                .maximumSize(maximumRecordSequenceNumbersToCache)
                .expireAfterWrite(sequenceNumberCacheTimeout, sequenceNumberCacheTimeoutUnit)
                .build(new CacheLoader<String, Optional<String>>() {
                    @Override
                    public Optional<String> load(String key) {
                        return Optional.<String>empty();
                    }
                });
    }

    @Override
    public void publish(AuthorUpdate authorUpdate) {
        ByteBuffer recordContent = ByteBuffer.wrap(asJsonString(authorUpdate).getBytes());
        if (this.logIndividualAuthorUpdates) {
            LOGGER.info(String.format("Publishing author update %s to Kinesis stream %s...", authorUpdate, kinesisStreamName));
        }

        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName(kinesisStreamName);
        putRecordRequest.setPartitionKey(authorUpdate.getAuthor().getId());
        putRecordRequest.setData(recordContent);
        perAuthorUpdateSequenceNumbers.getUnchecked(authorUpdate.getAuthor().getId()).ifPresent(putRecordRequest::setSequenceNumberForOrdering);

        PutRecordResult putRecordResult = amazonKinesisClient.putRecord(putRecordRequest);
        perAuthorUpdateSequenceNumbers.put(authorUpdate.getAuthor().getId(), Optional.of(putRecordResult.getSequenceNumber()));
    }

    private static String asJsonString(AuthorUpdate authorUpdate) {
        return new JSONObject()
                .put("id", authorUpdate.getId())
                .put("author", new JSONObject()
                        .put("id", authorUpdate.getAuthor().getId())
                        .put("name", authorUpdate.getAuthor().getName())
                        .put("removed", authorUpdate.getAuthor().isRemoved())
                )
                .toString();
    }

    private AWSCredentialsProvider createCredentialsProvider(SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration) {
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

    public static class SimulatedEnvironmentConfiguration {
        private final String awsRegion;
        private final String kinesisEndpoint;
        private final String awsAccessKey;
        private final String awsSecretKey;

        public SimulatedEnvironmentConfiguration(String awsRegion, String kinesisEndpoint, String awsAccessKey, String awsSecretKey) {
            this.awsRegion = awsRegion;
            this.kinesisEndpoint = kinesisEndpoint;
            this.awsAccessKey = awsAccessKey;
            this.awsSecretKey = awsSecretKey;
        }

        String getAwsRegion() {
            return awsRegion;
        }

        String getKinesisEndpoint() {
            return kinesisEndpoint;
        }

        String getAwsAccessKey() {
            return awsAccessKey;
        }

        String getAwsSecretKey() {
            return awsSecretKey;
        }
    }
}

