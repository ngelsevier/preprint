package com.ssrn.authors.postgres;

import com.ssrn.authors.domain.Author;
import com.ssrn.authors.postgres.test_decoding.TestDecodingLogicalReplicationSlotSource;
import com.ssrn.authors.postgres.test_decoding.TestDecodingLogicalReplicationStream;

import java.util.function.Consumer;

public class AuthorUpdateListener implements AutoCloseable {

    private final int index;
    private final JsonAuthorDeserializer jsonAuthorDeserializer;
    private final TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource;
    private LogicalReplicationStream logicalReplicationStream;

    AuthorUpdateListener(int index, JsonAuthorDeserializer jsonAuthorDeserializer, TestDecodingLogicalReplicationSlotSource testDecodingLogicalReplicationSlotSource) {
        this.index = index;
        this.jsonAuthorDeserializer = jsonAuthorDeserializer;
        this.testDecodingLogicalReplicationSlotSource = testDecodingLogicalReplicationSlotSource;
    }

    void onAuthorUpdated(Consumer<Author> authorConsumer) {
        logicalReplicationStream = new TestDecodingLogicalReplicationStream(index, testDecodingLogicalReplicationSlotSource);

        logicalReplicationStream.onReplicationMessage((checkpointer, replicationMessage) -> {
            if (replicationMessage.isInsertOrUpdateOnTable("public.author")) {
                Author author = jsonAuthorDeserializer.deserializeAuthorFromJson(replicationMessage.getValueInsertedInto("entity"));
                authorConsumer.accept(author);
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
