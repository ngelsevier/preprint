package com.ssrn.search.papers_consumer.fake_papers_service;

import java.util.UUID;

public class PaperServicePaperBuilder {

    private String title = "default title";
    private String[] authorIds = {"0"};
    private String id = UUID.randomUUID().toString();
    private boolean paperPrivate;
    private boolean paperIrrelevant;
    private boolean paperRestricted;
    private PaperServicePaper.SubmissionStage submissionStage;
    private String keywords;

    public static PaperServicePaperBuilder aPaper() {
        return new PaperServicePaperBuilder();
    }

    private PaperServicePaperBuilder() {
    }

    public PaperServicePaperBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PaperServicePaperBuilder withAuthorIds(String... authorIds) {
        this.authorIds = authorIds;
        return this;
    }

    public PaperServicePaperBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public PaperServicePaperBuilder thatIsPrivate() {
        this.paperPrivate = true;
        return this;
    }

    public PaperServicePaperBuilder withSubmissionStage(PaperServicePaper.SubmissionStage submissionStage) {
        this.submissionStage = submissionStage;
        return this;
    }

    public PaperServicePaperBuilder thatIsIrrelevant() {
        this.paperIrrelevant = true;
        return this;
    }

    public PaperServicePaperBuilder thatIsRestricted() {
        this.paperRestricted = true;
        return this;
    }

    public PaperServicePaper build() {
        return new PaperServicePaper(id, title, keywords, authorIds, paperPrivate, paperIrrelevant, paperRestricted, submissionStage);
    }

    public PaperServicePaperBuilder withKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
}
