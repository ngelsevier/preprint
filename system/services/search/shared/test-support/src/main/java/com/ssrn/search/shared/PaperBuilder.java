package com.ssrn.search.shared;

import com.ssrn.search.domain.Paper;
import com.ssrn.search.domain.SubmissionStage;

import java.util.UUID;

public class PaperBuilder {

    private String id = UUID.randomUUID().toString();
    private String title = "Default paper title";
    private String[] authorsIds = new String[]{"0"};
    private boolean isPrivate;
    private boolean isIrrelevant;
    private SubmissionStage submissionStage;
    private boolean isRestricted;
    private String keywords;

    static PaperBuilder aPaper() {
        return new PaperBuilder();
    }

    private PaperBuilder() {
    }

    public PaperBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PaperBuilder withAuthorIds(String... authorIds) {
        this.authorsIds = authorIds;
        return this;
    }

    public PaperBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public Paper build() {
        return new Paper(id, title, keywords, authorsIds, isPrivate, isIrrelevant, isRestricted, submissionStage);
    }

    public PaperBuilder isPrivate() {
        isPrivate = true;
        return this;
    }

    public PaperBuilder isPublic() {
        isPrivate = false;
        return this;
    }

    public PaperBuilder isIrrelevant() {
        isIrrelevant = true;
        return this;
    }

    public PaperBuilder isRelevant() {
        isIrrelevant = false;
        return this;
    }

    public PaperBuilder isRestricted() {
        isRestricted = true;
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
}
