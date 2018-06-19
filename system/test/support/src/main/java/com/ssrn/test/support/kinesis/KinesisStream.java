package com.ssrn.test.support.kinesis;

import com.amazonaws.services.kinesis.model.*;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;

public class KinesisStream extends KinesisStreamResource {

    public KinesisStream(String hostname, int kinesisPort, String streamName, int shardCount) {
        super(kinesisPort, hostname, streamName);
        recreateStream(shardCount);
    }

    public void evenlySplitNthOpenShard(int shardIndex) {
        List<Shard> shards = getShards();
        BigInteger parentShardEndingHashKey = new BigInteger(shards.get(0).getHashKeyRange().getEndingHashKey());
        BigInteger secondChildShardStartingHashKey = parentShardEndingHashKey.divide(new BigInteger("2"));

        Shard[] openShards = shards.stream()
                .filter(shard -> shard.getSequenceNumberRange().getEndingSequenceNumber() == null)
                .toArray(Shard[]::new);

        String parentShardId = openShards[shardIndex].getShardId();

        int initialShardCount = getShardCount();
        getAmazonKinesisClient().splitShard(getStreamName(), parentShardId, secondChildShardStartingHashKey.toString());

        waitUntil(() -> getShardCount() == initialShardCount + 2)
                .checkingEvery(100, TimeUnit.MILLISECONDS)
                .forNoMoreThan(10, TimeUnit.SECONDS);
    }

    public int getShardCount() {
        return getShards().size();
    }

    private List<Shard> getShards() {
        DescribeStreamResult describeStreamResult = getAmazonKinesisClient().describeStream(getStreamName());
        return describeStreamResult.getStreamDescription().getShards();
    }


    private void recreateStream(int shardCount) {
        try {
            getAmazonKinesisClient().deleteStream(getStreamName());
        } catch (com.amazonaws.services.kinesis.model.ResourceNotFoundException ignored) {
        }

        waitUntil(() -> {
            try {
                getAmazonKinesisClient().describeStream(getStreamName());
                return false;
            } catch (com.amazonaws.services.kinesis.model.ResourceNotFoundException ignored) {
                return true;
            }
        }).checkingEvery(100, TimeUnit.MILLISECONDS).forNoMoreThan(10, TimeUnit.SECONDS);


        getAmazonKinesisClient().createStream(getStreamName(), shardCount);

        waitUntil(() -> {
            try {
                DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
                describeStreamRequest.setStreamName(getStreamName());
                String exclusiveStartShardId = null;
                String streamStatus;
                int actualShardCount = 0;

                do {
                    describeStreamRequest.setExclusiveStartShardId(exclusiveStartShardId);
                    StreamDescription streamDescription = getAmazonKinesisClient().describeStream(describeStreamRequest).getStreamDescription();
                    streamStatus = streamDescription.getStreamStatus();
                    List<Shard> shards = streamDescription.getShards();
                    actualShardCount += shards.size();
                    if (streamDescription.getHasMoreShards() && shards.size() > 0) {
                        exclusiveStartShardId = shards.get(shards.size() - 1).getShardId();
                    } else {
                        exclusiveStartShardId = null;
                    }
                } while (exclusiveStartShardId != null);

                return "ACTIVE".equals(streamStatus) && actualShardCount == shardCount;

            } catch (ResourceNotFoundException ignored) {
                return false;
            }
        }).checkingEvery(100, TimeUnit.MILLISECONDS).forNoMoreThan(10, TimeUnit.SECONDS);
    }
}
