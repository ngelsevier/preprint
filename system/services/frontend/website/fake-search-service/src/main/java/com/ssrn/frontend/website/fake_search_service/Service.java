package com.ssrn.frontend.website.fake_search_service;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Service extends Application<ServiceConfiguration> {

    private final Queue<OverriddenResponse> overriddenResponses = new ConcurrentLinkedQueue<>();
    private final Queue<Boolean> overriddenReturnNewSearchResults = new ConcurrentLinkedQueue<>();
    private List<IndexedPaper> indexedPapers = new CopyOnWriteArrayList<>();
    private List<IndexedAuthor> indexedAuthors = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        new Service().run("server");
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        environment.jersey().register(new SearchResource(indexedPapers, indexedAuthors, overriddenResponses));
        environment.jersey().register(new HealthcheckResource());
        environment.jersey().register(new MetadataResource(indexedPapers, overriddenResponses, indexedAuthors, overriddenReturnNewSearchResults));
    }

}