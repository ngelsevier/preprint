package com.ssrn.test.support.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.commons.lang.NullArgumentException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static com.ssrn.test.support.logging.LoggingUtils.getLogbackLoggerNamed;

public class OverrideLogbackRootLoggerLevel extends TestWatcher {
    private static final Logger ROOT_LOGGER = getLogbackLoggerNamed(Logger.ROOT_LOGGER_NAME);

    private final Level overriddenLevel;
    private Level originalLevel;

    public OverrideLogbackRootLoggerLevel(Level overriddenLevel) {
        if (overriddenLevel == null) {
            throw new NullArgumentException("overriddenLevel");
        }

        this.overriddenLevel = overriddenLevel;
    }

    @Override
    protected void starting(Description description) {
        originalLevel = ROOT_LOGGER.getLevel();
        ROOT_LOGGER.setLevel(overriddenLevel);
    }

    @Override
    protected void finished(Description description) {
        ROOT_LOGGER.setLevel(originalLevel);
    }

}
