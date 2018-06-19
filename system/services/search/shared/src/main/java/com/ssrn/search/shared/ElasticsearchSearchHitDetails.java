package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticsearchSearchHitDetails {
    private ElasticsearchSearchHitsSource elasticsearchSearchHitsSource;
    private HashMap<String, String[]> elasticsearchSearchHitsHighlight;

    @JsonCreator
    public ElasticsearchSearchHitDetails(
            @JsonProperty("_source") ElasticsearchSearchHitsSource elasticsearchSearchHitsSource,
            @JsonProperty("highlight") HashMap<String, String[]> elasticsearchSearchHitsHighlight) {

        this.elasticsearchSearchHitsSource = elasticsearchSearchHitsSource;
        this.elasticsearchSearchHitsHighlight = elasticsearchSearchHitsHighlight;
    }

    public ElasticsearchSearchHitsSource getSource() {
        return elasticsearchSearchHitsSource;
    }

    public HashMap<String, String[]> getHighlights() {
        return elasticsearchSearchHitsHighlight;
    }
}
