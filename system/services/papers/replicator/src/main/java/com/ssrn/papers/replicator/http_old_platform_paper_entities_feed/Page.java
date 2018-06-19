package com.ssrn.papers.replicator.http_old_platform_paper_entities_feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

    private List<Paper> papers;

    public Page(@JsonProperty("papers") List<Paper> papers) {
        this.papers = papers;
    }

    public List<Paper> getPapers() {
        return papers;
    }
}
