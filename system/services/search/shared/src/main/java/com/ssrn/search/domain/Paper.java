package com.ssrn.search.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paper {

    private final String id;
    private String title;
    private String keywords;
    private String[] authorIds;
    private boolean paperPrivate;
    private final boolean paperIrrelevant;
    private final boolean paperRestricted;
    private final SubmissionStage submissionStage;

    @JsonCreator
    public Paper(@JsonProperty(value = "id", required = true) String id,
                 @JsonProperty(value = "title", required = true) String title,
                 @JsonProperty(value = "keywords") String keywords,
                 @JsonProperty(value = "authorIds") String[] authorIds,
                 @JsonProperty(value = "paperPrivate", required = true) boolean paperPrivate,
                 @JsonProperty(value = "paperIrrelevant", required = true) boolean paperIrrelevant,
                 @JsonProperty(value = "paperRestricted", required = true) boolean paperRestricted,
                 @JsonProperty(value = "submissionStage", required = true) SubmissionStage submissionStage) {
        this.id = id;
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

    public String getKeywords() {
        return keywords;
    }

    public String getId() {
        return id;
    }

    public String[] getAuthorIds() {
        return authorIds;
    }

    public boolean isPaperPrivate() {
        return paperPrivate;
    }

    public boolean isPaperIrrelevant() {
        return paperIrrelevant;
    }

    public SubmissionStage getSubmissionStage() {
        return submissionStage;
    }

    public boolean isPaperRestricted() {
        return paperRestricted;
    }

    public boolean isPaperSearchable() {
        return !(paperPrivate || paperIrrelevant || paperRestricted || !submissionStage.isPaperSearchable());
    }

    @Override
    public String toString() {
        return "Paper{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", keywords='" + keywords + '\'' +
                ", authorIds=" + Arrays.toString(authorIds) +
                ", paperPrivate=" + paperPrivate +
                ", paperIrrelevant=" + paperIrrelevant +
                ", paperRestricted=" + paperRestricted +
                ", submissionStage=" + submissionStage +
                '}';
    }
}