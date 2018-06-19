package com.ssrn.search.shared;

import org.junit.Before;
import org.junit.BeforeClass;

import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ElasticsearchTest {
    private static final ElasticsearchCluster elasticsearchCluster = new ElasticsearchCluster("localhost", 9200);

    public static final String ELASTICSEARCH_PAPERS_INDEX_NAME = "papers-integration-tests";
    public static final String ELASTICSEARCH_AUTHORS_INDEX_NAME = "authors-integration-tests";


    @BeforeClass
    public static void waitUntilElasticSearchAvailable() {
        waitUntil(elasticsearchCluster::isAvailable)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(30, SECONDS);
    }

    @Before
    public void resetElasticsearch() {
        elasticsearchCluster().recreateIndex(ELASTICSEARCH_PAPERS_INDEX_NAME);
        elasticsearchCluster().recreateIndex(ELASTICSEARCH_AUTHORS_INDEX_NAME);
    }

    ElasticsearchCluster elasticsearchCluster() {
        return elasticsearchCluster;
    }
}
