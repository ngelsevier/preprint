package com.ssrn.fake_old_platform;

import java.util.concurrent.atomic.AtomicInteger;

class SequentialIdGenerator {
    private final AtomicInteger nextNewId;

    SequentialIdGenerator(int initialId) {
        this.nextNewId = new AtomicInteger(initialId);
    }

    int getNextId() {
        return nextNewId.getAndAdd(1);
    }
}
