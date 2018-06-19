package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddHistoricPaperRequest {
    private final String title;

    private final String keywords;

    private String[] authorIds;
    private boolean paperPrivate;
    private boolean paperIrrelevant;
    private String submissionStage;
    private boolean paperRestricted;
    @JsonCreator
    AddHistoricPaperRequest(@JsonProperty(value = "title", required = true) String title,
                            @JsonProperty(value = "keywords") String keywords,
                            @JsonProperty(value = "authorIds") String[] authorIds,
                            @JsonProperty(value = "paperPrivate") boolean paperPrivate,
                            @JsonProperty(value = "paperIrrelevant") boolean paperIrrelevant,
                            @JsonProperty(value = "paperRestricted") boolean paperRestricted,
                            @JsonProperty(value = "submissionStage") String submissionStage
                            ) {
        this.title = title;
        this.keywords = keywords;
        this.authorIds = authorIds;
        this.paperPrivate = paperPrivate;
        this.paperIrrelevant = paperIrrelevant;
        this.paperRestricted = paperRestricted;
        this.submissionStage = submissionStage;
    }


    public String getTitle() {
        return title;
    }

    public String getKeywords() { return keywords; }

    public String[] getAuthorIds() {
        return authorIds;
    }

    public boolean isPaperPrivate() {
        return paperPrivate;
    }

    public boolean isPaperIrrelevant() {
        return paperIrrelevant;
    }

    public boolean isPaperRestricted() { return paperRestricted; }

    public String getSubmissionStage() {
        return submissionStage;
    }
}
