package com.ssrn.frontend.website.fake_search_service;

import ch.qos.logback.classic.Level;
import io.dropwizard.logging.DefaultLoggingFactory;

public class ServiceConfiguration extends io.dropwizard.Configuration {
    public ServiceConfiguration() {
        DefaultLoggingFactory loggingFactory = new DefaultLoggingFactory();
        loggingFactory.setLevel(Level.INFO.toString());
        setLoggingFactory(loggingFactory);
    }
}
