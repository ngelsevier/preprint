package com.ssrn.papers.replicator.concurrency;

import com.ssrn.papers.replicator.ThrowingRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class SingleJobRunner {

    private final Semaphore semaphore = new Semaphore(1);

    public boolean run(ThrowingRunnable runnable, Consumer<Throwable> exceptionConsumer) {
        if (!semaphore.tryAcquire()) {
            return false;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                runnable.run();
            } catch (RuntimeException runtimeException) {
                throw runtimeException;
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }

            return null;
        }).exceptionally(e -> {
            exceptionConsumer.accept(e);
            return null;
        }).thenAccept(ignored -> semaphore.release());

        return true;
    }
}
