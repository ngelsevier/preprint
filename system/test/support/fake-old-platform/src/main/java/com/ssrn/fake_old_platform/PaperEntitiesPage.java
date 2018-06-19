package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaperEntitiesPage {

    private List<Paper> papers;

    @JsonCreator
    PaperEntitiesPage(@JsonProperty(value = "papers", required = true) List<Paper> papers) {
        this.papers = papers;
    }

    public List<Paper> getPapers() {
        return papers;
    }
}
