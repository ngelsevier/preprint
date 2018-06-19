package com.ssrn.papers.postgres;

import java.util.List;

public interface LogicalReplicationSlotSource {
    LogicalReplicationSlot getSlotWithIndex(int index);

    List<LogicalReplicationSlot> getSlots();
}
