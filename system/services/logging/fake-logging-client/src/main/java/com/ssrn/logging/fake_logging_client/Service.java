package com.ssrn.logging.fake_logging_client;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class Service extends Application<ServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new LogsResource());
        environment.jersey().register(new HealthcheckResource());
        environment.jersey().register(new UnhandledExceptionMapper());
    }
}
