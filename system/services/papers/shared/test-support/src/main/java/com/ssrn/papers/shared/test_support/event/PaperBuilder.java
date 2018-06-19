package com.ssrn.papers.shared.test_support.event;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.SubmissionStage;

import java.util.Random;

public class PaperBuilder {

    private int version = 1;
    private String id = Integer.toString(new Random().nextInt(99999));
    private String title = "Default paper title";
    private String[] authorIds = new String[]{};
    private boolean paperPrivate;
    private boolean paperIrrelevant;
    private boolean paperRestricted;
    private SubmissionStage submissionStage;
    private String keywords;

    public static PaperBuilder aPaper() {
        return new PaperBuilder();
    }

    private PaperBuilder() {
    }

    public PaperBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PaperBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public PaperBuilder withVersion(int version) {
        this.version = version;
        return this;
    }

    public PaperBuilder withAuthorIds(String... authorIds) {
        this.authorIds = authorIds;
        return this;
    }

    public PaperBuilder withPaperPrivate(boolean isPrivate) {
        this.paperPrivate = isPrivate;
        return this;
    }

    public PaperBuilder withPaperIrrelevant(boolean paperIrrelevant) {
        this.paperIrrelevant = paperIrrelevant;
        return this;
    }

    public PaperBuilder withPaperRestricted(boolean paperRestricted) {
        this.paperRestricted = paperRestricted;
        return this;
    }

    public PaperBuilder withSubmissionStage(SubmissionStage submissionStage) {
        this.submissionStage = submissionStage;
        return this;
    }

    public PaperBuilder withKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    public Paper build() {
        return new Paper(id, version, title, keywords, authorIds, paperPrivate, paperIrrelevant, paperRestricted, submissionStage);
    }
}
