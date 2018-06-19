package com.ssrn.test.support.utils;

import java.util.concurrent.TimeUnit;

public class Interval {
    private final int interval;
    private final TimeUnit intervalUnit;

    public static Interval checkingEvery(int interval, TimeUnit intervalUnit) {
        return new Interval(interval, intervalUnit);
    }

    private Interval(int interval, TimeUnit intervalUnit) {
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    public int getValue() {
        return interval;
    }

    public TimeUnit getUnit() {
        return intervalUnit;
    }
}
