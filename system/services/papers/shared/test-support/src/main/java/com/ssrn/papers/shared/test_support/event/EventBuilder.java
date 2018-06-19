package com.ssrn.papers.shared.test_support.event;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.SubmissionStage;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public class EventBuilder {

    public static DraftedEventBuilder aDraftedEvent() {
        return new DraftedEventBuilder();
    }

    public static UnrecognisedEventBuilder anUnrecognisedEvent() {
        return new UnrecognisedEventBuilder();
    }

    public static MadePrivateEventBuilder aMadePrivateEvent() {
        return new MadePrivateEventBuilder();
    }

    public static MadePublicEventBuilder aMadePublicEvent() {
        return new MadePublicEventBuilder();
    }

    public static ConsideredIrrelevantEventBuilder aConsideredIrrelevantEvent() {
        return new ConsideredIrrelevantEventBuilder();
    }

    public static ConsideredRelevantEventBuilder aConsideredRelevantEvent() {
        return new ConsideredRelevantEventBuilder();
    }

    public static RestrictedEventBuilder aRestrictedEvent() {
        return new RestrictedEventBuilder();
    }

    public static UnrestrictedEventBuilder anUnrestrictedEvent() {
        return new UnrestrictedEventBuilder();
    }

    public static SubmissionStageChangedEventBuilder aSubmissionStageChangedEvent() {
        return new SubmissionStageChangedEventBuilder();
    }

    public static TitleChangedEventBuilder aTitleChangedEvent() {
        return new TitleChangedEventBuilder();
    }

    public static KeywordsChangedEventBuilder aKeywordsChangedEvent() {
        return new KeywordsChangedEventBuilder();
    }

    public static AuthorChangedEventBuilder anAuthorChangedEvent() {
        return new AuthorChangedEventBuilder();
    }

    public static class DraftedEventBuilder {
        private boolean isPrivate;
        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();
        private boolean isIrrelevant;
        private String title;
        private Integer[] authorIds;
        private boolean isRestricted;
        private boolean isTolerated;

        DraftedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(1);
        }

        public DraftedEventBuilder withPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }

        public DraftedEventBuilder withIrrelevant(boolean isIrrelevant) {
            this.isIrrelevant = isIrrelevant;
            return this;
        }

        public DraftedEventBuilder withRestricted(boolean isRestricted) {
            this.isRestricted = isRestricted;
            return this;
        }

        public DraftedEventBuilder withTolerated(boolean isTolerated) {
            this.isTolerated = isTolerated;
            return this;
        }

        public DraftedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public DraftedEventBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public DraftedEventBuilder withAuthorIds(Integer... authorIds) {
            this.authorIds = authorIds;
            return this;
        }

        public Paper.DraftedEvent build() {
            return new Paper.DraftedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp(),
                    title,
                    Arrays.stream(authorIds).map(x -> Integer.toString(x)).toArray(String[]::new),
                    isPrivate,
                    isIrrelevant,
                    isRestricted,
                    isTolerated);
        }
    }

    public static class TitleChangedEventBuilder {
        private String title = String.format("default title %s", UUID.randomUUID());
        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        TitleChangedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public TitleChangedEventBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public TitleChangedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.TitleChangedEvent build() {
            return new Paper.TitleChangedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp(),
                    title
            );
        }
    }

    public static class AuthorChangedEventBuilder {
        private Integer[] authorIds = new Integer[]{1};
        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        AuthorChangedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public AuthorChangedEventBuilder withAuthorIds(Integer... authorIds) {
            this.authorIds = authorIds;
            return this;
        }

        public AuthorChangedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.AuthorChangedEvent build() {
            return new Paper.AuthorChangedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp(),
                    Arrays.stream(authorIds).map(x -> Integer.toString(x)).toArray(String[]::new)
            );
        }
    }

    public static class MadePrivateEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        MadePrivateEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public MadePrivateEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.MadePrivateEvent build() {
            return new Paper.MadePrivateEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }

    }

    public static class MadePublicEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        MadePublicEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public MadePublicEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.MadePublicEvent build() {
            return new Paper.MadePublicEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }

    }

    public static class ConsideredIrrelevantEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        ConsideredIrrelevantEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public ConsideredIrrelevantEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.ConsideredIrrelevantEvent build() {
            return new Paper.ConsideredIrrelevantEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }
    }

    public static class ConsideredRelevantEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        ConsideredRelevantEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public ConsideredRelevantEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.ConsideredRelevantEvent build() {
            return new Paper.ConsideredRelevantEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }
    }

    public static class RestrictedEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        RestrictedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public RestrictedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.RestrictedEvent build() {
            return new Paper.RestrictedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }
    }

    public static class UnrestrictedEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        UnrestrictedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public UnrestrictedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.UnrestrictedEvent build() {
            return new Paper.UnrestrictedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }
    }

    public static class KeywordsChangedEventBuilder {

        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();
        private String keywords;

        KeywordsChangedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public KeywordsChangedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public Paper.KeywordsChangedEvent build() {
            return new Paper.KeywordsChangedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp(),
                    keywords
            );
        }

        public KeywordsChangedEventBuilder withKeywords(String keywords) {
            this.keywords = keywords;
            return this;
        }
    }

    public static class SubmissionStageChangedEventBuilder {
        private SubmissionStage submissionStage;
        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        SubmissionStageChangedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public SubmissionStageChangedEventBuilder withStandardEventProperties(Consumer<CommonPropertiesBuilder> commonPropertiesConfigurer) {
            commonPropertiesConfigurer.accept(commonPropertiesBuilder);
            return this;
        }

        public SubmissionStageChangedEventBuilder withSubmissionStage(SubmissionStage submissionStage) {
            this.submissionStage = submissionStage;
            return this;
        }


        public Paper.SubmissionStageChangedEvent build() {
            return new Paper.SubmissionStageChangedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp(),
                    submissionStage
            );
        }
    }

    public static class UnrecognisedEventBuilder {
        private final CommonPropertiesBuilder commonPropertiesBuilder = new CommonPropertiesBuilder();

        UnrecognisedEventBuilder() {
            commonPropertiesBuilder.withEntityVersion(2);
        }

        public Paper.UnrecognisedEvent build() {
            return new Paper.UnrecognisedEvent(
                    commonPropertiesBuilder.getId(),
                    commonPropertiesBuilder.getEntityId(),
                    commonPropertiesBuilder.getEntityVersion(),
                    commonPropertiesBuilder.getEventTimestamp()
            );
        }
    }

    public static class CommonPropertiesBuilder {
        private String id = UUID.randomUUID().toString();
        private String entityId = UUID.randomUUID().toString();
        private int entityVersion = 1;
        private DateTime eventTimestamp = DateTime.now();

        public CommonPropertiesBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public CommonPropertiesBuilder withEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public CommonPropertiesBuilder withEntityVersion(int entityVersion) {
            this.entityVersion = entityVersion;
            return this;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getId() {
            return id;
        }

        public int getEntityVersion() {
            return entityVersion;
        }

        public DateTime getEventTimestamp() {
            return eventTimestamp;
        }


    }
}
