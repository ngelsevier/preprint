package com.ssrn.search.papers_consumer.fake_papers_service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;

import java.nio.ByteBuffer;

class KinesisStreamClient {
    private final AmazonKinesis amazonKinesisClient;
    private String lastSequenceNumber = null;

    KinesisStreamClient(KinesisClientConfiguration configuration) {
        this.amazonKinesisClient = createAmazonKinesisClient(configuration);
    }

    void writeRecordToStream(String streamName, String partitionKey, String data) {
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName(streamName);
        putRecordRequest.setPartitionKey(partitionKey);
        putRecordRequest.setData(ByteBuffer.wrap(data.getBytes()));

        if (lastSequenceNumber != null) {
            putRecordRequest.setSequenceNumberForOrdering(lastSequenceNumber);
        }

        PutRecordResult result = amazonKinesisClient.putRecord(putRecordRequest);

        lastSequenceNumber = result.getSequenceNumber();
    }

    static AmazonKinesis createAmazonKinesisClient(KinesisClientConfiguration configuration) {
        String serviceEndpoint = String.format("%s:%d", configuration.getKinesisEndpoint(), configuration.getKinesisPort());

        return AmazonKinesisClientBuilder.standard()
                .withCredentials(configuration.getCredentialsProvider())
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, configuration.getRegion()))
                .build();
    }
}
