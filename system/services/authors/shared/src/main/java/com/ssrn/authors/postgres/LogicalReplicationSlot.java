package com.ssrn.authors.postgres;

public interface LogicalReplicationSlot {
    ReplicationSlotStream createStream();

    int getIndex();

    void drop();
}
