package com.ssrn.authors.utils;

public class ExceptionUtils {
    public static void throwAsUncheckedException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new RuntimeException(throwable);
        }
    }
}
