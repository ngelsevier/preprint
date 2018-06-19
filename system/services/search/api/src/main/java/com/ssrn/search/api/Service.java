package com.ssrn.search.api;

import com.ssrn.search.shared.ElasticsearchLibrary;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;


public class Service extends Application<ServiceConfiguration> {

    private static final int DEFAULT_SCROLL_SIZE = 100;

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        ElasticsearchLibrary library = new ElasticsearchLibrary("papers", "authors", "localhost", 9200, DEFAULT_SCROLL_SIZE);

        environment.jersey().register(new HealthcheckResource());
        environment.jersey().register(new SearchResource(library));
        environment.jersey().register(new UnhandledExceptionMapper());
    }
}
