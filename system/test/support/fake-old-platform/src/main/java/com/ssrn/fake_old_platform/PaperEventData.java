package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;


public class PaperEventData implements EventData {

    private String title;
    private String keywords;
    private int[] authorIds;
    private boolean paperPrivate;
    private boolean paperIrrelevant;
    private boolean paperRestricted;
    private boolean paperTolerated;
    private String submissionStage;

    @JsonCreator()
    public PaperEventData(@JsonProperty("title") String title,
                          @JsonProperty("keywords") String keywords,
                          @JsonProperty("authorIds") int[] authorIds,
                          @JsonProperty("isPrivate") boolean paperPrivate,
                          @JsonProperty("isConsideredIrrelevant") boolean paperIrrelevant,
                          @JsonProperty("isRestricted") boolean paperRestricted,
                          @JsonProperty("isTolerated") boolean paperTolerated,
                          @JsonProperty("submissionStage") String submissionStage) {
        this.title = title;
        this.keywords = keywords;
        this.authorIds = authorIds;
        this.paperPrivate = paperPrivate;
        this.paperIrrelevant = paperIrrelevant;
        this.paperRestricted = paperRestricted;
        this.paperTolerated = paperTolerated;
        this.submissionStage = submissionStage;
    }

    PaperEventData(String title, String keywords, String submissionStage) {
        this.title = title;
        this.keywords = keywords;
        this.submissionStage = submissionStage;
    }

    PaperEventData(int[] authorIds) {
        this.authorIds = authorIds;
    }

    public PaperEventData(String title, boolean paperPrivate, boolean paperIrrelevant, boolean paperRestricted, boolean paperTolerated, int[] authorIds) {
        this.title = title;
        this.paperPrivate = paperPrivate;
        this.paperIrrelevant = paperIrrelevant;
        this.paperRestricted = paperRestricted;
        this.paperTolerated = paperTolerated;
        this.authorIds = authorIds;
    }

    /**
     * A hack to replicate existing platform behaviour for DRAFTED event
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getTitle() {
        try {
            return Integer.parseInt(title);
        } catch (NumberFormatException e) {
            return title;
        }
    }

    @JsonProperty("keywords")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getKeywords() {
        return keywords;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public int[] getAuthorIds() {
        return authorIds;
    }

    @JsonProperty("isPrivate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean isPaperPrivate() {
        return paperPrivate;
    }

    @JsonProperty("isConsideredIrrelevant")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean isPaperIrrelevant() {
        return paperIrrelevant;
    }

    @JsonProperty("isRestricted")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean isPaperRestricted() {
        return paperRestricted;
    }

    @JsonProperty("isTolerated")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean isPaperTolerated() {
        return paperTolerated;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSubmissionStage() {
        return submissionStage;
    }

    @Override
    public String toString() {
        return "PaperEventData{" +
                "title='" + title + '\'' +
                ", authorIds=" + Arrays.toString(authorIds) +
                ", paperPrivate=" + paperPrivate +
                ", paperIrrelevant=" + paperIrrelevant +
                ", paperRestricted=" + paperRestricted +
                ", paperTolerated=" + paperTolerated +
                ", submissionStage='" + submissionStage + '\'' +
                '}';
    }
}
