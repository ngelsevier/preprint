package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateSubmissionStageRequest {
    private final String submissionStage;

    private final Integer id;

    @JsonCreator
    UpdateSubmissionStageRequest(@JsonProperty(value = "paperId", required = true) Integer id,
                                 @JsonProperty(value = "submissionStage", required = true) String submissionStage) {
        this.id = id;
        this.submissionStage = submissionStage;
    }


    public Integer getId() {
        return id;
    }

    public String getSubmissionStage() {
        return submissionStage;
    }
}
