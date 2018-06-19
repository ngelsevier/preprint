package com.ssrn.test.support.kinesis;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.Record;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import static com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream.LATEST;
import static com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream.TRIM_HORIZON;
import static com.ssrn.test.support.utils.ThreadingUtils.onABackgroundThreadRun;

public class KinesisClient extends KinesisStreamResource implements AutoCloseable {
    private final EventRecordProcessorThatStoresRecord eventRecordProcessorThatStoresRecord;
    private final String hostname;
    private final int dynamodDbPort;
    private final boolean ignoreExistingEvents;
    private Worker worker;
    private AmazonDynamoDB amazonDynamoDbClient;

    public KinesisClient(String hostname, int kinesisPort, String streamName, int dynamodDbPort, boolean ignoreExistingEvents) {
        super(kinesisPort, hostname, streamName);
        this.hostname = hostname;
        this.dynamodDbPort = dynamodDbPort;
        this.ignoreExistingEvents = ignoreExistingEvents;
        this.eventRecordProcessorThatStoresRecord = getEventRecordProcessorThatStoresRecord(streamName);
    }

    public List<Record> records() {
        return eventRecordProcessorThatStoresRecord.receivedRecords();
    }

    public void forgetReceivedRecords() {
        eventRecordProcessorThatStoresRecord.forgetRecords();
    }

    @Override
    public void close() {
        if (worker != null) {
            worker.shutdown();
        }
    }

    private EventRecordProcessorThatStoresRecord getEventRecordProcessorThatStoresRecord(String kinesisStreamName) {
        String kclApplicationName = String.format("%s-%s", KinesisClient.class.getName(), UUID.randomUUID());

        KinesisClientLibConfiguration kinesisClientLibConfiguration = new KinesisClientLibConfiguration(
                kclApplicationName,
                kinesisStreamName,
                getCredentialsProvider(),
                getWorkerId()
        ).withInitialPositionInStream(ignoreExistingEvents ? LATEST : TRIM_HORIZON);

        EventRecordProcessorThatStoresRecord eventRecordProcessorThatStoresRecord = new EventRecordProcessorThatStoresRecord();

        worker = new Worker.Builder()
                .dynamoDBClient(getAmazonDynamoDB())
                .kinesisClient(getAmazonKinesisClient())
                .cloudWatchClient(new NoopAmazonCloudWatch())
                .recordProcessorFactory(() -> eventRecordProcessorThatStoresRecord)
                .config(kinesisClientLibConfiguration)
                .build();

        onABackgroundThreadRun(worker);

        if (ignoreExistingEvents) {
            while (eventRecordProcessorThatStoresRecord.receivedRecords().size() == 0) {
                String entityId = "test";
                String recordContent = new JSONObject()
                        .put("id", entityId)
                        .put("title", "TEST")
                        .toString();

                PutRecordRequest putRecordRequest = new PutRecordRequest();
                putRecordRequest.setStreamName(kinesisStreamName);
                putRecordRequest.setPartitionKey(entityId);
                putRecordRequest.setData(ByteBuffer.wrap(recordContent.getBytes()));

                getAmazonKinesisClient().putRecord(putRecordRequest);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }

            eventRecordProcessorThatStoresRecord.forgetRecords();
        }

        return eventRecordProcessorThatStoresRecord;
    }

    private AmazonDynamoDB getAmazonDynamoDB() {
        if (amazonDynamoDbClient == null) {
            globallyDisableAwsSdkCertificateChecking();
            globalDisableAwsCborWireProtocol();

            amazonDynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(getCredentialsProvider())
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                            String.format("http://%s:%d", hostname, dynamodDbPort),
                            getAwsRegion()))
                    .build();
        }

        return amazonDynamoDbClient;
    }


    private static String getWorkerId() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
