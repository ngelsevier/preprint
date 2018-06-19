package com.ssrn.test.support.golden_data;

public class SsrnPaper {
    private final boolean isPrivate;
    private final boolean isIrrelevant;
    private final boolean isRestricted;
    private final SsrnUser[] authors;
    private final String title;
    private final String id;
    private String submissionStage;
    private String keywords;

    SsrnPaper(String id, String title, String keywords, boolean isPrivate, boolean isIrrelevant, boolean isRestricted, String submissionStage, SsrnUser... authors) {
        this.id = id;
        this.title = title;
        this.keywords = keywords;
        this.isPrivate = isPrivate;
        this.isIrrelevant = isIrrelevant;
        this.isRestricted = isRestricted;
        this.submissionStage = submissionStage;
        this.authors = authors;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SsrnUser[] getAuthors() {
        return authors;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isIrrelevant() {
        return isIrrelevant;
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public String getSubmissionStage() {
        return submissionStage;
    }

    public String getKeywords() {
        return keywords;
    }
}
