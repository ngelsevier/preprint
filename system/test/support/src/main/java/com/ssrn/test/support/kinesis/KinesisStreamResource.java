package com.ssrn.test.support.kinesis;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;

import static com.amazonaws.SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY;

public class KinesisStreamResource {
    private static final String awsRegion = "us-east-1";
    private static final String awsAccessKey = "dummy";
    private static final String awsSecretKey = "dummy";

    private final String hostname;
    private final int kinesisPort;
    private final String streamName;
    private AmazonKinesis amazonKinesisClient;

    KinesisStreamResource(int kinesisPort, String hostname, String streamName) {
        this.kinesisPort = kinesisPort;
        this.hostname = hostname;
        this.streamName = streamName;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getKinesisEndpoint() {
        return String.format("https://%s:%d", hostname, kinesisPort);
    }

    AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }

    static void globallyDisableAwsSdkCertificateChecking() {
        System.getProperties().put(DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
    }

    static void globalDisableAwsCborWireProtocol() {
        System.getProperties().put(AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    }

    AmazonKinesis getAmazonKinesisClient() {
        if (amazonKinesisClient == null) {
            globallyDisableAwsSdkCertificateChecking();
            globalDisableAwsCborWireProtocol();

            amazonKinesisClient = AmazonKinesisClientBuilder.standard()
                    .withCredentials(getCredentialsProvider())
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(getKinesisEndpoint(), getAwsRegion()))
                    .build();
        }

        return amazonKinesisClient;
    }

}
