package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Paper {

    private final int id;
    private String title;
    private String keywords;
    private int[] authorIds;
    private int version;
    private final List<PaperEvent> unsavedEvents = new ArrayList<>();
    private boolean paperPrivate;
    private boolean consideredIrrelevant;
    private String submissionStage;
    private boolean paperRestricted;
    private boolean paperTolerated;
    private int originalAbstractId;

    public Paper(int id, String title, String keywords, boolean historicPaper, boolean paperPrivate, boolean paperIrrelevant, boolean paperRestricted, boolean paperTolerated, int[] authorIds, String submissionStage) {
        this(id, title, keywords, historicPaper, null, paperPrivate, paperIrrelevant, paperRestricted, paperTolerated, authorIds, submissionStage);
    }

    public Paper(int id, String title, String keywords, boolean historicPaper, boolean paperPrivate, boolean paperIrrelevant, boolean paperRestricted, String submissionStage) {
        this(id, title, keywords, historicPaper, null, paperPrivate, paperIrrelevant, paperRestricted, false, new int[0], submissionStage);
    }

    Paper(int id, String title, String keywords, boolean historicPaper, DateTime overriddenEventTime, boolean paperPrivate, boolean paperIrrelevant, boolean paperRestricted, boolean paperTolerated, int[] authorIds, String submissionStage) {
        this(id, title, keywords, authorIds, paperPrivate, paperIrrelevant, paperRestricted, paperTolerated, submissionStage);

        if (historicPaper) {
            version = 1;
        } else {
            overriddenEventTime = overriddenEventTime == null ? new DateTime(DateTimeZone.UTC) : overriddenEventTime;
            emitEvent("DRAFTED", new PaperEventData(title, paperPrivate, paperIrrelevant, paperRestricted, paperTolerated, authorIds), overriddenEventTime);
        }
    }

    @JsonCreator
    public Paper(@JsonProperty("id") int id,
                 @JsonProperty("title") String title,
                 @JsonProperty("keywords") String keywords,
                 @JsonProperty("authorIds") int[] authorIds,
                 @JsonProperty("isPrivate") boolean paperPrivate,
                 @JsonProperty("isConsideredIrrelevant") boolean consideredIrrelevant,
                 @JsonProperty("isRestricted") boolean paperRestricted,
                 @JsonProperty("isTolerated") boolean paperTolerated,
                 @JsonProperty("submissionStage") String submissionStage) {
        this.id = id;
        this.title = title;
        this.keywords = keywords;
        this.authorIds = authorIds;
        this.paperPrivate = paperPrivate;
        this.consideredIrrelevant = consideredIrrelevant;
        this.paperRestricted = paperRestricted;
        this.paperTolerated = paperTolerated;
        this.submissionStage = submissionStage;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getKeywords() {
        return keywords;
    }

    public int[] getAuthorIds() {
        return authorIds;
    }

    public int getVersion() {
        return version;
    }

    @JsonProperty("isPrivate")
    public boolean isPaperPrivate() {
        return paperPrivate;
    }

    @JsonProperty("isConsideredIrrelevant")
    public boolean isConsideredIrrelevant() {
        return consideredIrrelevant;
    }

    @JsonProperty("isRestricted")
    public boolean isPaperRestricted() {
        return paperRestricted;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSubmissionStage() {
        return submissionStage;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", keywords='" + keywords + '\'' +
                ", authorIds=" + Arrays.toString(authorIds) +
                ", version=" + version +
                ", unsavedEvents=" + unsavedEvents +
                ", paperPrivate=" + paperPrivate +
                ", consideredIrrelevant=" + consideredIrrelevant +
                ", submissionStage='" + submissionStage + '\'' +
                ", paperRestricted=" + paperRestricted +
                ", paperTolerated=" + paperTolerated +
                ", submissionStageIsSubmitted=" + submissionStageIsSubmitted() +
                '}';
    }

    public void changeTitleTo(String title) {
        changeTitleTo(title, null);
    }

    public void changeTitleTo(String title, DateTime overriddenEventTime) {
        this.title = title;
        emitEvent("TITLE CHANGED", new PaperEventData(title, null, null), overriddenEventTime);
    }

    public void changeKeywordsTo(String keywords) {
        changeKeywordsTo(keywords, null);
    }

    public void changeKeywordsTo(String keywords, DateTime overriddenEventTime) {
        this.keywords = keywords;
        emitEvent("KEYWORDS CHANGED", new PaperEventData(null, keywords, null), overriddenEventTime);
    }

    void addAuthor(String authorId, DateTime overriddenEventTime, boolean isHistoricAssociation) {
        if (!Arrays.stream(authorIds).anyMatch(id -> id == Integer.parseInt(authorId))) {
            authorIds = ArrayUtils.addAll(authorIds, Integer.parseInt(authorId));
        }

        if (!isHistoricAssociation) {
            emitEvent("AUTHOR CHANGED", new PaperEventData(authorIds), overriddenEventTime);
        }
    }

    void removeAuthor(String authorId) {
        authorIds = Arrays.stream(authorIds).filter(i -> i != Integer.parseInt(authorId)).toArray();
        emitEvent("AUTHOR CHANGED", new PaperEventData(authorIds), null);
    }

    void reorderAuthors(int[] authorIds) {
        this.authorIds = authorIds;
        emitEvent("AUTHOR CHANGED", new PaperEventData(authorIds), null);
    }

    public void makePaperPublic() {
        paperPrivate = false;
        emitEvent("MADE PUBLIC", null, null);
    }

    public void makePaperPrivate() {
        paperPrivate = true;
        emitEvent("MADE PRIVATE", null, null);
    }

    public void considerPaperIrrelevant() {
        consideredIrrelevant = true;
        emitEvent("CONSIDERED IRRELEVANT", null, null);
    }

    public void considerPaperRelevant() {
        consideredIrrelevant = false;
        emitEvent("CONSIDERED RELEVANT", null, null);
    }

    public void makePaperRestricted() {
        paperRestricted = true;
        emitEvent("RESTRICTED", null, null);
    }

    public void makePaperUnrestricted() {
        paperRestricted = false;
        emitEvent("UNRESTRICTED", null, null);
    }

    public void submissionStageChangedTo(String submissionStage) {
        this.submissionStage = submissionStage;
        emitEvent("SUBMISSION STAGE CHANGED", new PaperEventData(null, null, submissionStage), null);
    }

    public boolean submissionStageIsSubmitted() {
        return "SUBMITTED".equals(submissionStage);
    }

    public boolean submissionStageIsApproved() {
        return "APPROVED".equals(submissionStage) || "APPROVED-RESTRICTED".equals(submissionStage);
    }

    public void setSubmissionStage(String submissionStage) {
        this.submissionStage = submissionStage;
    }

    public boolean isPaperTolerated() {
        return paperTolerated;
    }

    public void setOriginalAbstractId(int originalAbstractId) {
        this.originalAbstractId = originalAbstractId;
    }

    public int getOriginalAbstractId() {
        return originalAbstractId;
    }

    public void delete() {
        emitEvent("DELETED", null, null);
    }

    private void emitEvent(String type, PaperEventData data, DateTime overriddenEventTime) {
        unsavedEvents.add(new PaperEvent(
                UUID.randomUUID().toString(),
                type,
                Integer.toString(id),
                overriddenEventTime == null ? new DateTime(DateTimeZone.UTC) : overriddenEventTime,
                ++version,
                data
        ));
    }

    void appendUnsavedEventsTo(EventLog paperEventLog) {
        unsavedEvents.forEach(paperEventLog::append);
        unsavedEvents.clear();
    }

    public boolean hasSearchableSubmissionStage() {
        return submissionStage.equals("IN DRAFT") || submissionStage.equals("SUBMITTED") || submissionStage.equals("APPROVED") || submissionStage.equals("UNDER REVIEW");
    }
}
