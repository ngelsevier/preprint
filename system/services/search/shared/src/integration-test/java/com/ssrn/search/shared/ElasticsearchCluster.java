package com.ssrn.search.shared;

class ElasticsearchCluster {

    private final int nodePort;
    private final String nodeHostname;
    private final HttpElasticsearchIndexAdminClient httpElasticsearchIndexAdminClient;

    ElasticsearchCluster(String nodeHostname, int nodePort) {
        this.nodePort = nodePort;
        this.nodeHostname = nodeHostname;
        httpElasticsearchIndexAdminClient = new HttpElasticsearchIndexAdminClient(nodeHostname, nodePort);
    }

    void recreateIndex(String indexName) {
        if (httpElasticsearchIndexAdminClient.doesIndexExist(indexName)) {
            httpElasticsearchIndexAdminClient.deleteIndex(indexName);
        }

        httpElasticsearchIndexAdminClient.createIndex(indexName);
    }

    int nodePort() {
        return nodePort;
    }

    String nodeHostname() {
        return nodeHostname;
    }

    public Boolean isAvailable() {
        return httpElasticsearchIndexAdminClient.isElasticsearchAvailable();
    }
}
