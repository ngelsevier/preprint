package com.ssrn.test.support.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RepeatingTask implements AutoCloseable {

    private final AtomicBoolean loop = new AtomicBoolean();
    private final long interval;
    private final Runnable runnable;
    private final TimeUnit intervalUnit;

    public static FluentSyntax repeat(Runnable runnable) {
        return new FluentSyntax(runnable);
    }

    private RepeatingTask(Runnable runnable, long interval, TimeUnit intervalUnit) {
        this.runnable = runnable;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    public void start() {
        loop.set(true);

        new Thread(() -> {
            while (loop.get()) {
                this.runnable.run();

                try {
                    Thread.sleep(intervalUnit.toMillis(interval));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void close() {
        loop.set(false);
    }

    public static class FluentSyntax {
        private Runnable runnable;

        FluentSyntax(Runnable runnable) {
            this.runnable = runnable;
        }

        public RepeatingTask every(long interval, TimeUnit intervalUnit) {
            return new RepeatingTask(runnable, interval, intervalUnit);
        }
    }
}
