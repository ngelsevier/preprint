package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticsearchSearchHit {
    int total;
    ElasticsearchSearchHitDetails[] elasticsearchSearchHitDetails;

    @JsonCreator
    public ElasticsearchSearchHit(@JsonProperty("total") int total, @JsonProperty("hits") ElasticsearchSearchHitDetails[] elasticsearchSearchHitDetails) {
        this.total = total;
        this.elasticsearchSearchHitDetails = elasticsearchSearchHitDetails;
    }

    public int getTotal() {
        return total;
    }

    public ElasticsearchSearchHitDetails[] getElasticsearchSearchHitDetails() {
        return elasticsearchSearchHitDetails;
    }
}
