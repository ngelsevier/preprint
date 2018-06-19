package com.ssrn.search.papers_consumer.fake_papers_service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public class KinesisClientConfiguration {

    private final String region;
    private final String kinesisEndpoint;
    private final int kinesisPort;
    private final AWSCredentialsProvider awsCredentialsProvider;

    public KinesisClientConfiguration(boolean simulatedEnvironment, String region, String kinesisEndpoint, int kinesisPort) {
        this.region = region;
        this.kinesisEndpoint = kinesisEndpoint;
        this.kinesisPort = kinesisPort;
        this.awsCredentialsProvider = simulatedEnvironment ?
                new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy")) :
                DefaultAWSCredentialsProviderChain.getInstance();
    }

    public String getRegion() {
        return region;
    }

    public String getKinesisEndpoint() {
        return kinesisEndpoint;
    }

    public int getKinesisPort() {
        return kinesisPort;
    }

    public AWSCredentialsProvider getCredentialsProvider() {
        return awsCredentialsProvider;
    }

}
