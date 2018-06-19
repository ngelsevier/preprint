package com.ssrn.shared.concurrency;

import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

public class RetryUtils {
    public static void waitUntil(Supplier<Boolean> condition, Runnable onTimeout, int timeoutSeconds) {
        long timeoutTime = System.currentTimeMillis() + SECONDS.toMillis(timeoutSeconds);

        while (!condition.get()) {
            if (System.currentTimeMillis() > timeoutTime) {
                onTimeout.run();
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
