package com.ssrn.papers.replicator.http_old_platform_paper_entities_feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssrn.papers.domain.SubmissionStage;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paper {

    private final int id;
    private final String title;
    private final int version;
    private String[] authorIds;
    private boolean paperPrivate;
    private String submissionStage;
    private boolean isConsideredIrrelevant;
    private boolean paperRestricted;
    private String keywords;

    public Paper(@JsonProperty(value = "id", required = true) int id,
                 @JsonProperty(value = "title", required = true) String title,
                 @JsonProperty(value = "keywords") String keywords,
                 @JsonProperty(value = "authorIds") String[] authorIds,
                 @JsonProperty(value = "version", required = true) int version,
                 @JsonProperty(value = "isPrivate") boolean paperPrivate,
                 @JsonProperty(value = "isConsideredIrrelevant") boolean isConsideredIrrelevant,
                 @JsonProperty(value = "isRestricted", required = true) boolean paperRestricted,
                 @JsonProperty(value = "submissionStage") String submissionStage) {
        this.id = id;
        this.title = title;
        this.keywords = keywords;
        this.authorIds = authorIds;
        this.version = version;
        this.paperPrivate = paperPrivate;
        this.isConsideredIrrelevant = isConsideredIrrelevant;
        this.submissionStage = submissionStage;
        this.paperRestricted = paperRestricted;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public String[] getAuthorIds() {
        return authorIds;
    }

    public boolean isPaperPrivate() {
        return paperPrivate;
    }

    public boolean isConsideredIrrelevant() {
        return isConsideredIrrelevant;
    }

    public boolean isPaperRestricted() {
        return paperRestricted;
    }

    public String getSubmissionStage() {
        return submissionStage;
    }

    public String getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", version=" + version +
                ", authorIds=" + Arrays.toString(authorIds) +
                ", paperPrivate=" + paperPrivate +
                ", submissionStage='" + submissionStage + '\'' +
                ", isConsideredIrrelevant=" + isConsideredIrrelevant +
                ", paperRestricted=" + paperRestricted +
                ", keywords='" + keywords + '\'' +
                '}';
    }
}
