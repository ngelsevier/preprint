package com.ssrn.papers.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.function.Consumer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paper {

    private final String id;
    private String title;
    private String keywords;
    private String[] authorIds;
    private int version;
    private boolean paperPrivate;
    private boolean paperIrrelevant;
    private boolean paperRestricted;
    private SubmissionStage submissionStage;

    @JsonCreator
    public Paper(@JsonProperty(value = "id", required = true) String id,
                 @JsonProperty(value = "version", required = true) int version,
                 @JsonProperty(value = "title", required = true) String title,
                 @JsonProperty(value = "keywords") String keywords,
                 @JsonProperty("authorIds") String[] authorIds,
                 @JsonProperty(value = "paperPrivate", required = true) boolean paperPrivate,
                 @JsonProperty(value = "paperIrrelevant", required = true) boolean paperIrrelevant,
                 @JsonProperty(value = "paperRestricted", required = true) boolean paperRestricted,
                 @JsonProperty(value = "submissionStage", required = true) SubmissionStage submissionStage) {
        this.id = id;
        this.title = title;
        this.keywords = keywords;
        this.authorIds = authorIds;
        this.version = version;
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

    public int getVersion() {
        return version;
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

    @Override
    public String toString() {
        return "Paper{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", keywords='" + keywords + '\'' +
                ", authorIds=" + Arrays.toString(authorIds) +
                ", version=" + version +
                ", paperPrivate=" + paperPrivate +
                ", paperIrrelevant=" + paperIrrelevant +
                ", paperRestricted=" + paperRestricted +
                ", submissionStage=" + submissionStage +
                '}';
    }

    public void apply(MadePublicEvent madePublicEvent) {
        apply(madePublicEvent, e -> paperPrivate = false);
    }

    public void apply(MadePrivateEvent madePrivateEvent) {
        apply(madePrivateEvent, e -> paperPrivate = true);
    }

    private void apply(ConsideredIrrelevantEvent consideredIrrelevantEvent) {
        apply(consideredIrrelevantEvent, e -> paperIrrelevant = true);
    }

    private void apply(ConsideredRelevantEvent consideredRelevantEvent) {
        apply(consideredRelevantEvent, e -> paperIrrelevant = false);
    }

    private void apply(RestrictedEvent restrictedEvent) {
        apply(restrictedEvent, e -> paperRestricted = true);
    }

    private void apply(UnrestrictedEvent unrestrictedEvent) {
        apply(unrestrictedEvent, e-> paperRestricted = false);

    }

    public void apply(AuthorChangedEvent authorChangedEvent) {
        apply(authorChangedEvent, e -> authorIds = e.getAuthorIds());
    }

    public void apply(TitleChangedEvent titleChangedEvent) {
        apply(titleChangedEvent, e -> title = e.getTitle());
    }

    public void apply(KeywordsChangedEvent keywordsChangedEvent) {
        apply(keywordsChangedEvent, e -> keywords = e.getKeywords());
    }

    public void apply(SubmissionStageChangedEvent submissionStageChangedEvent) {
        apply(submissionStageChangedEvent, e -> submissionStage = e.getSubmissionStage());
    }

    public void apply(UnrecognisedEvent unrecognisedEvent) {
        apply(unrecognisedEvent, e -> {
        });
    }

    protected <TEvent extends Event> void apply(TEvent event, Consumer<TEvent> eventProjector) {
        int currentVersion = getVersion();

        if (event.getEntityVersion() != currentVersion + 1) {
            throw new UnexpectedEntityVersionEventAppliedException(event, currentVersion);
        }

        eventProjector.accept(event);

        this.version = event.getEntityVersion();
    }

    public static abstract class Event {
        private final String id;
        private final String type;
        private final String data;
        private final String entityId;
        private final int entityVersion;

        private final DateTime entityTimestamp;

        public Event(String id, String entityId, int entityVersion, String type, String data, DateTime entityTimestamp) {
            this.id = id;
            this.entityId = entityId;
            this.entityVersion = entityVersion;
            this.type = type;
            this.data = data;
            this.entityTimestamp = entityTimestamp;
        }

        public String getId() {
            return id;
        }

        public String getEntityId() {
            return entityId;
        }

        public int getEntityVersion() {
            return entityVersion;
        }

        public String getData() {
            return data;
        }

        public DateTime getEntityTimestamp() {
            return entityTimestamp;
        }

        public abstract void applyOn(Paper paper);

    }

    public static class AuthorChangedEvent extends Event {


        private final String[] authorIds;

        public AuthorChangedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp, String[] authorIds) {
            super(id, entityId, entityVersion, "AUTHOR CHANGED", null, entityTimestamp);
            this.authorIds = authorIds;
        }

        @Override
        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        public String[] getAuthorIds() {
            return authorIds;
        }

        @Override
        public String toString() {
            return "AuthorChangedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    ", authorIds=" + Arrays.toString(authorIds) +
                    '}';
        }

    }

    public static class DraftedEvent extends Event {

        private final boolean paperPrivate;

        private final boolean paperIrrelevant;
        private String title;
        private String[] authorIds;
        private boolean paperRestricted;
        private boolean paperTolerated;


        public DraftedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp, String title, String[] authorIds, boolean paperPrivate, boolean paperIrrelevant, boolean paperRestricted, boolean paperTolerated) {
            super(id, entityId, entityVersion, "DRAFTED", null, entityTimestamp);
            this.paperPrivate = paperPrivate;
            this.paperIrrelevant = paperIrrelevant;
            this.title = title;
            this.authorIds = authorIds;
            this.paperRestricted = paperRestricted;
            this.paperTolerated = paperTolerated;
        }

        public boolean isPaperPrivate() {
            return paperPrivate;
        }

        public boolean isPaperIrrelevant() {
            return paperIrrelevant;
        }

        public String getTitle() {
            return title;
        }

        public String[] getAuthorIds() {
            return authorIds;
        }

        public boolean isPaperRestricted() {
            return paperRestricted;
        }

        public boolean isPaperTolerated() {
            return paperTolerated;
        }

        @Override
        public void applyOn(Paper paper) {
            throw new RuntimeException("Attempting to apply a DRAFTED event to an existing paper");
        }

        @Override
        public String toString() {
            return "DraftedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    ", title='" + title + '\'' +
                    ", authorIds=" + Arrays.toString(authorIds) +
                    ", paperPrivate=" + paperPrivate +
                    ", paperIrrelevant=" + paperIrrelevant +
                    ", paperRestricted=" + paperRestricted +
                    ", paperTolerated=" + paperTolerated +
                    ", data='" + getData() + '\'' +
                    '}';
        }
    }

    public static class MadePrivateEvent extends Event {


        public MadePrivateEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "MADE PRIVATE", null, entityTimestamp);
        }

        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "MadePrivateEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }

    }

    public static class MadePublicEvent extends Event {


        public MadePublicEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "MADE PUBLIC", null, entityTimestamp);
        }

        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "MadePublicEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }

    }

    public static class ConsideredIrrelevantEvent extends Event {

        public ConsideredIrrelevantEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "CONSIDERED IRRELEVANT", null, entityTimestamp);
        }

        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "ConsideredIrrelevantEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }
    }

    public static class ConsideredRelevantEvent extends Event {

        public ConsideredRelevantEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "CONSIDERED RELEVANT", null, entityTimestamp);
        }

        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "ConsideredRelevantEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }
    }

    public static class RestrictedEvent extends Event {

        public RestrictedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "RESTRICTED", null, entityTimestamp);
        }

        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "RestrictedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }
    }

    public static class UnrestrictedEvent extends Event {

        public UnrestrictedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "UNRESTRICTED", null, entityTimestamp);
        }

        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "UnrestrictedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }

    }

    public static class TitleChangedEvent extends Event {


        private final String title;

        public TitleChangedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp, String title) {
            super(id, entityId, entityVersion, "TITLE CHANGED", null, entityTimestamp);
            this.title = title;
        }

        @Override
        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return "TitleChangedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    public static class SubmissionStageChangedEvent extends Event {
        private final SubmissionStage submissionStage;

        public SubmissionStageChangedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp, SubmissionStage submissionStage) {
            super(id, entityId, entityVersion, "SUBMISSION STAGE CHANGED", null, entityTimestamp);
            this.submissionStage = submissionStage;
        }

        @Override
        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        public SubmissionStage getSubmissionStage() {
            return submissionStage;
        }

        @Override
        public String toString() {
            return "SubmissionStageChangedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    ", submisstionStage='" + submissionStage + '\'' +
                    '}';
        }
    }

    public static class UnrecognisedEvent extends Event {


        public UnrecognisedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp) {
            super(id, entityId, entityVersion, "UNRECOGNISED TYPE", null, entityTimestamp);
        }

        @Override
        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        @Override
        public String toString() {
            return "UnrecognisedEvent{" +
                    "id='" + getId() + '\'' +
                    ", entityId='" + getEntityId() + '\'' +
                    ", entityVersion=" + getEntityVersion() +
                    ", data='" + getData() + '\'' +
                    ", entityTimestamp=" + getEntityTimestamp() +
                    '}';
        }

    }

    public static class KeywordsChangedEvent extends Event {

        private final String keywords;

        public KeywordsChangedEvent(String id, String entityId, int entityVersion, DateTime entityTimestamp, String keywords) {
            super(id, entityId, entityVersion, "KEYWORDS CHANGED", null, entityTimestamp);
            this.keywords = keywords;
        }

        @Override
        public void applyOn(Paper paper) {
            paper.apply(this);
        }

        public String getKeywords() {
            return keywords;
        }
    }

}