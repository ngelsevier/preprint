package com.ssrn.test.support.utils;

import java.util.concurrent.TimeUnit;

public class ThreadingUtils {

    public static void onABackgroundThread(Runnable workToDo) {
        onABackgroundThreadRun(workToDo);
    }

    public static void onABackgroundThreadRun(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void sleepFor(long sleep, TimeUnit timeUnit) {
        try {
            Thread.sleep(timeUnit.toMillis(sleep));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
