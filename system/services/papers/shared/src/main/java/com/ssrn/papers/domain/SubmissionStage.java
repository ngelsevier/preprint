package com.ssrn.papers.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubmissionStage {
    REJECTED("REJECTED"),
    APPROVED("APPROVED"),
    DELETED("DELETED"),
    SUBMITTED("SUBMITTED"),
    UNDER_REVIEW("UNDER REVIEW"),
    IN_DRAFT("IN DRAFT");

    String name;

    SubmissionStage(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public static SubmissionStage fromString(String name) {
        for (SubmissionStage s : values()) {
            if (name.equals(s.name)) {
                return s;
            }
        }
        return null;
    }
}
