package com.ssrn.papers.postgres;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Interval {
    private final int interval;
    private final TimeUnit intervalUnit;

    public static Interval checkingEvery(int interval, TimeUnit intervalUnit) {
        return new Interval(interval, intervalUnit);
    }

    public static Interval checkingEvery100Milliseconds() {
        return checkingEvery(100, MILLISECONDS);
    }

    private Interval(int interval, TimeUnit intervalUnit) {
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    int getValue() {
        return interval;
    }

    TimeUnit getUnit() {
        return intervalUnit;
    }
}

