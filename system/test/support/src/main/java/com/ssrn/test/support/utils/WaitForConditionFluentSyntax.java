package com.ssrn.test.support.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WaitForConditionFluentSyntax {
    private final Supplier<Boolean> condition;

    private WaitForConditionFluentSyntax(Supplier<Boolean> condition) {
        this.condition = condition;
    }

    public static WaitForConditionFluentSyntax waitUntil(Supplier<Boolean> condition) {
        return new WaitForConditionFluentSyntax(condition);
    }

    public FluentSyntax1 checkingEvery(long interval, TimeUnit intervalUnit) {
        return new FluentSyntax1(condition, intervalUnit, interval);
    }

    public FluentSyntax1 checkingAsFastAsPossible() {
        return new FluentSyntax1(condition, TimeUnit.MILLISECONDS, 1);
    }

    public static class FluentSyntax1 {
        private final TimeUnit intervalUnit;
        private final long interval;
        private final Supplier<Boolean> condition;

        FluentSyntax1(Supplier<Boolean> condition, TimeUnit intervalUnit, long interval) {
            this.intervalUnit = intervalUnit;
            this.interval = interval;
            this.condition = condition;
        }

        public void forNoMoreThan(int timeout, TimeUnit timeoutUnit) {
            long startTime = System.currentTimeMillis();

            boolean conditionMet = false;

            while (System.currentTimeMillis() - startTime < timeoutUnit.toMillis(timeout)) {
                if (condition.get()) {
                    conditionMet = true;
                    break;
                }

                try {
                    Thread.sleep(intervalUnit.toMillis(interval));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!conditionMet) {
                throw new RuntimeException(String.format("Condition was not true within %d %s", timeout, timeoutUnit));
            }
        }
    }
}
