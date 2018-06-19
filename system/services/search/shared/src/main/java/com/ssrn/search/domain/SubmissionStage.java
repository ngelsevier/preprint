package com.ssrn.search.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubmissionStage {
    REJECTED("REJECTED", false),
    APPROVED("APPROVED", true),
    DELETED("DELETED", false),
    SUBMITTED("SUBMITTED", true),
    UNDER_REVIEW("UNDER REVIEW", true),
    IN_DRAFT("IN DRAFT", false);

    String name;
    boolean paperSearchable;

    SubmissionStage(String name, boolean paperSearchable) {
        this.name = name;
        this.paperSearchable = paperSearchable;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public boolean isPaperSearchable(){
        return paperSearchable;
    }

}
