package com.ssrn.test.support.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.ssrn.test.support.utils.RepeatingTask.repeat;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TaskWhilstAnotherTaskRepeats {
    public static FluentSyntax1 whilstRepeating(Runnable runnableToRepeat) {
        return new FluentSyntax1(runnableToRepeat);
    }

    public static class FluentSyntax1 {
        private final Runnable runnableToRepeat;

        FluentSyntax1(Runnable runnableToRepeat) {
            this.runnableToRepeat = runnableToRepeat;
        }

        public WaitForConditionFluentSyntax waitUntil(Supplier<Boolean> condition) {
            return new WaitForConditionFluentSyntax(condition, runnableToRepeat);
        }

        public RunTaskFluentSyntax every(int interval, TimeUnit intervalUnit) {
            return new RunTaskFluentSyntax(runnableToRepeat, interval, intervalUnit);
        }

        public static class WaitForConditionFluentSyntax {
            private final Supplier<Boolean> condition;
            private final Runnable runnableToRepeat;

            public WaitForConditionFluentSyntax(Supplier<Boolean> condition, Runnable runnableToRepeat) {
                this.condition = condition;
                this.runnableToRepeat = runnableToRepeat;
            }

            public void forNoMoreThan(int timeout, TimeUnit seconds) {
                try (RepeatingTask repeatingTask = repeat(runnableToRepeat).every(100, MILLISECONDS)) {
                    repeatingTask.start();
                    com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil(condition)
                            .checkingEvery(100, MILLISECONDS)
                            .forNoMoreThan(timeout, seconds);
                }
            }
        }

        public static class RunTaskFluentSyntax {
            private final Runnable runnableToRepeat;
            private final int interval;
            private final TimeUnit intervalUnit;

            RunTaskFluentSyntax(Runnable runnableToRepeat, int interval, TimeUnit intervalUnit) {
                this.runnableToRepeat = runnableToRepeat;
                this.interval = interval;
                this.intervalUnit = intervalUnit;
            }

            public void run(Runnable runnable) {
                try (RepeatingTask repeatingTask = RepeatingTask.repeat(runnableToRepeat).every(interval, intervalUnit)) {
                    repeatingTask.start();
                    runnable.run();
                }
            }

        }
    }
}
