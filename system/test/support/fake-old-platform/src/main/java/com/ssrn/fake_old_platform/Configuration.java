package com.ssrn.fake_old_platform;

import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;

import static java.util.Arrays.asList;

public class Configuration extends io.dropwizard.Configuration {
    public Configuration() {
        HttpConnectorFactory httpConnectorFactory = new HttpConnectorFactory();
        httpConnectorFactory.setPort(80);

        DefaultServerFactory serverFactory = new DefaultServerFactory();
        serverFactory.setApplicationConnectors(asList(httpConnectorFactory));

        setServerFactory(serverFactory);
    }
}
