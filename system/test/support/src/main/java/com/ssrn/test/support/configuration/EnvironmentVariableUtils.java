package com.ssrn.test.support.configuration;

public class EnvironmentVariableUtils {
    public static String getEnvironmentVariableOrDefaultTo(String defaultValue, String name) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
