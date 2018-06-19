package com.ssrn.papers.postgres;

import java.util.function.BiConsumer;

public interface LogicalReplicationStream extends AutoCloseable {
    void onReplicationMessage(BiConsumer<Checkpointer, LogicalReplicationMessage> replicationMessageConsumer);

    void close();
}
