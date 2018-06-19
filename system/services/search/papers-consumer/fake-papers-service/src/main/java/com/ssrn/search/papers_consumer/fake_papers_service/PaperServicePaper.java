package com.ssrn.search.papers_consumer.fake_papers_service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class PaperServicePaper {
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
    private final String id;

    private final String title;
    private String keywords;
    private String[] authorIds;
    private final boolean paperPrivate;
    private final boolean paperIrrelevant;
    private final boolean paperRestricted;
    private final SubmissionStage submissionStage;
    public PaperServicePaper(@JsonProperty(value = "id", required = true) String id,
                             @JsonProperty(value = "title", required = true) String title,
                             @JsonProperty(value = "keywords") String keywords,
                             @JsonProperty(value = "authorIds") String[] authorIds,
                             @JsonProperty(value = "paperPrivate") boolean paperPrivate,
                             @JsonProperty(value = "paperIrrelevant") boolean paperIrrelevant,
                             @JsonProperty(value = "paperRestricted") boolean paperRestricted,
                             @JsonProperty(value = "submissionStage") SubmissionStage submissionStage) {
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    public boolean isPaperRestricted() {
        return paperRestricted;
    }

    public SubmissionStage getSubmissionStage() {
        return submissionStage;
    }
}
