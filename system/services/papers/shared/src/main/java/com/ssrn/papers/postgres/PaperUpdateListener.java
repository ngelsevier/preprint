package com.ssrn.papers.postgres;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.postgres.test_decoding.TestDecodingLogicalReplicationSlotSource;
import com.ssrn.papers.postgres.test_decoding.TestDecodingLogicalReplicationStream;

import java.util.function.Consumer;

public class PaperUpdateListener implements AutoCloseable {

    private final int index;
    private final JsonPaperDeserializer jsonPaperDeserializer;
    private final TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource;
    private LogicalReplicationStream logicalReplicationStream;

    PaperUpdateListener(int index, JsonPaperDeserializer jsonPaperDeserializer, TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource) {
        this.index = index;
        this.jsonPaperDeserializer = jsonPaperDeserializer;
        this.testDecodingLogicalReplicationSlotSource = testDecodingLogicalReplicationSlotSource;
    }

    void onPaperUpdated(Consumer<Paper> paperConsumer) {
        logicalReplicationStream = new TestDecodingLogicalReplicationStream(index, testDecodingLogicalReplicationSlotSource);

        logicalReplicationStream.onReplicationMessage((checkpointer, replicationMessage) -> {
            if (replicationMessage.isInsertOrUpdateOnTable("public.paper")) {
                Paper paper = jsonPaperDeserializer.deserializePaperFromJson(replicationMessage.getValueInsertedInto("entity"));
                paperConsumer.accept(paper);
            } else if (replicationMessage.isTransactionCommit()) {
                checkpointer.checkpoint(replicationMessage.getLogSequenceNumber());
            }
        });
    }

    @Override
    public void close() {
        if (logicalReplicationStream != null) {
            logicalReplicationStream.close();
        }
    }
}
