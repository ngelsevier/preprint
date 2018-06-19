package com.ssrn.papers.emitter;

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
import com.ssrn.papers.domain.Paper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.amazonaws.SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY;

class KinesisPapersSink implements PapersSink {

    private final static Logger LOGGER = LoggerFactory.getLogger(KinesisPapersSink.class.getName());

    private final LoadingCache<String, Optional<String>> perPaperSequenceNumbers;

    private final String kinesisStreamName;
    private final AmazonKinesis amazonKinesisClient;
    private boolean logIndividualPapers;

    KinesisPapersSink(SimulatedEnvironmentConfiguration simulatedEnvironmentConfiguration, String kinesisStreamName, boolean logIndividualPapers, int maximumRecordSequenceNumbersToCache, int sequenceNumberCacheTimeout, TimeUnit sequenceNumberCacheTimeoutUnit) {
        this.kinesisStreamName = kinesisStreamName;
        this.logIndividualPapers = logIndividualPapers;
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

        perPaperSequenceNumbers = CacheBuilder.newBuilder()
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
    public void streamPaper(Paper paper) {
        ByteBuffer recordContent = ByteBuffer.wrap(asJsonString(paper).getBytes());
        if (this.logIndividualPapers) {
            LOGGER.info(String.format("Publishing paper %s to Kinesis stream %s...", paper, kinesisStreamName));
        }

        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName(kinesisStreamName);
        putRecordRequest.setPartitionKey(paper.getId());
        putRecordRequest.setData(recordContent);
        perPaperSequenceNumbers.getUnchecked(paper.getId()).ifPresent(putRecordRequest::setSequenceNumberForOrdering);

        PutRecordResult putRecordResult = amazonKinesisClient.putRecord(putRecordRequest);
        perPaperSequenceNumbers.put(paper.getId(), Optional.of(putRecordResult.getSequenceNumber()));
    }

    private static String asJsonString(Paper paper) {
        return new JSONObject()
                .put("id", paper.getId())
                .put("title", paper.getTitle())
                .put("keywords", paper.getKeywords())
                .put("authorIds", paper.getAuthorIds())
                .put("paperPrivate", paper.isPaperPrivate())
                .put("paperIrrelevant", paper.isPaperIrrelevant())
                .put("paperRestricted", paper.isPaperRestricted())
                .put("submissionStage", paper.getSubmissionStage() != null ? paper.getSubmissionStage().getName() : null)
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

    static class SimulatedEnvironmentConfiguration {
        private final String awsRegion;
        private final String kinesisEndpoint;
        private final String awsAccessKey;
        private final String awsSecretKey;

        SimulatedEnvironmentConfiguration(String awsRegion, String kinesisEndpoint, String awsAccessKey, String awsSecretKey) {
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
