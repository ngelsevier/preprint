package com.ssrn.papers.postgres;

public interface LogicalReplicationSlot {
    ReplicationSlotStream createStream();

    int getIndex();

    void drop();
}
