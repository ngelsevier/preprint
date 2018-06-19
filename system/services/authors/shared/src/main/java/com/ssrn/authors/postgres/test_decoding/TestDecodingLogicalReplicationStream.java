package com.ssrn.authors.postgres.test_decoding;

import com.ssrn.authors.postgres.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class TestDecodingLogicalReplicationStream implements LogicalReplicationStream {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestDecodingLogicalReplicationStream.class);

    private final AtomicBoolean checkForNewReplicationMessages = new AtomicBoolean();
    private final AtomicBoolean listenForReplicationMessages = new AtomicBoolean();
    private final int index;
    private final TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource;

    public TestDecodingLogicalReplicationStream(int index, TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource) {
        this.index = index;
        this.testDecodingLogicalReplicationSlotSource = testDecodingLogicalReplicationSlotSource;
    }

    @Override
    public void onReplicationMessage(BiConsumer<Checkpointer, LogicalReplicationMessage> replicationMessageConsumer) {
        listenForReplicationMessages.set(true);

        while (listenForReplicationMessages.get()) {
            checkForNewReplicationMessages.set(true);

            LogicalReplicationSlot replicationSlot = testDecodingLogicalReplicationSlotSource.getSlotWithIndex(index);

            try (ReplicationSlotStream replicationSlotStream = replicationSlot.createStream()) {
                Checkpointer checkpointer = replicationSlotStream.getCheckpointer();

                while (checkForNewReplicationMessages.get()) {
                    ByteBuffer replicationMessageContent = null;

                    try {
                        replicationMessageContent = replicationSlotStream.tryAndGetPendingReplicationMessage();
                    } catch (Throwable throwable1) {
                        LOGGER.error("Exception thrown whilst polling for replication message", throwable1);
                        checkForNewReplicationMessages.set(false);
                    }

                    if (replicationMessageContent == null) {
                        sleepForMilliseconds(100);
                    } else {
                        TestDecodingLogicalReplicationMessage testDecodingLogicalReplicationMessage = new TestDecodingLogicalReplicationMessage(asString(replicationMessageContent), replicationSlotStream.getLastReceiveLSN());

                        try {
                            replicationMessageConsumer.accept(checkpointer, testDecodingLogicalReplicationMessage);
                        } catch (Throwable throwable) {
                            LOGGER.error(
                                    String.format("Exception thrown whilst handling replication message, WAL position %s",
                                            testDecodingLogicalReplicationMessage.getLogSequenceNumber()
                                    ),
                                    new ReplicationException(testDecodingLogicalReplicationMessage.getContent(), throwable)
                            );

                            checkForNewReplicationMessages.set(false);
                        }
                    }
                }
            }
        }
    }

    private static void sleepForMilliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String asString(ByteBuffer replicationMessageContent) {
        int offset = replicationMessageContent.arrayOffset();
        byte[] bytes = replicationMessageContent.array();
        int length = bytes.length - offset;
        return new String(bytes, offset, length);
    }

    @Override
    public void close() {
        listenForReplicationMessages.set(false);
        checkForNewReplicationMessages.set(false);
    }

    private class ReplicationException extends Throwable {
        ReplicationException(String content, Throwable throwable) {
            super(content.replace(":", "-"), throwable);
        }
    }
}
