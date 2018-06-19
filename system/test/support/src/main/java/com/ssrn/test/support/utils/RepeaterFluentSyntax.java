package com.ssrn.test.support.utils;

import java.util.stream.IntStream;

public class RepeaterFluentSyntax {
    private final Runnable taskToRepeat;

    public static RepeaterFluentSyntax repeat(Runnable runnable) {
        return new RepeaterFluentSyntax(runnable);
    }

    public void times(int repeatCount) {
        IntStream.range(0, repeatCount).forEach(i -> taskToRepeat.run());
    }

    private RepeaterFluentSyntax(Runnable taskToRepeat) {
        this.taskToRepeat = taskToRepeat;
    }
}
