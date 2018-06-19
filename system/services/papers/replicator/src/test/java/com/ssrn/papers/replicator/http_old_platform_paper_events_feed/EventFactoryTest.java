package com.ssrn.papers.replicator.http_old_platform_paper_events_feed;


import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.SubmissionStage;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsArrayContaining;
import org.hamcrest.collection.IsArrayContainingInOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Ignore;
import org.junit.Test;

import static com.ssrn.papers.domain.SubmissionStage.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class EventFactoryTest {

    private final JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);

    @Test
    public void shouldMapDraftedEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        ObjectNode publicRelevantUnRestrictedUnToleratedWithTitleData = jsonNodeFactory.objectNode();
        publicRelevantUnRestrictedUnToleratedWithTitleData
                .put("isPrivate", false)
                .put("isConsideredIrrelevant", false)
                .put("isRestricted", false)
                .put("isTolerated", false)
                .put("title", 123456)
                .putArray("authorIds").add(1001).add(1002);

        Event publicIrrelevantRestrictedUnToleratedWithTitleOldPlatformPaperEvent = new Event("public id", "the entity id", "DRAFTED", publicRelevantUnRestrictedUnToleratedWithTitleData, 1, "2018-02-23T13:29:47.914768000Z");

        ObjectNode privateIrrelevantRestrictedToleratedWithTitleData = jsonNodeFactory.objectNode();
        privateIrrelevantRestrictedToleratedWithTitleData
                .put("isPrivate", true)
                .put("isConsideredIrrelevant", true)
                .put("isRestricted", true)
                .put("isTolerated", true)
                .put("title", "abcdef")
                .putArray("authorIds").add(1003).add(1004);

        Event privateRelevantUnrestrictedToleratedWithTitleOldPlatformPaperEvent = new Event("private id", "the entity id", "DRAFTED", privateIrrelevantRestrictedToleratedWithTitleData, 1, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event publicDomainPaperEvent = eventFactory.map(publicIrrelevantRestrictedUnToleratedWithTitleOldPlatformPaperEvent);
        Paper.Event privateDomainPaperEvent = eventFactory.map(privateRelevantUnrestrictedToleratedWithTitleOldPlatformPaperEvent);

        // Then
        assertThat(publicDomainPaperEvent, is(instanceOf(Paper.DraftedEvent.class)));
        Paper.DraftedEvent publicDraftedEvent = (Paper.DraftedEvent) publicDomainPaperEvent;
        assertThat(publicDraftedEvent.getId(), is(equalTo("public id")));
        assertThat(publicDraftedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(publicDraftedEvent.getEntityVersion(), is(equalTo(1)));
        assertThat(publicDraftedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
        assertThat(publicDraftedEvent.isPaperPrivate(), is(equalTo(false)));
        assertThat(publicDraftedEvent.isPaperIrrelevant(), is(equalTo(false)));
        assertThat(publicDraftedEvent.isPaperRestricted(), is(equalTo(false)));
        assertThat(publicDraftedEvent.isPaperTolerated(), is(equalTo(false)));
        assertThat(publicDraftedEvent.getTitle(), is(equalTo("123456")));
        assertThat(publicDraftedEvent.getAuthorIds(), is(IsArrayContainingInOrder.arrayContaining("1001", "1002")));

        assertThat(privateDomainPaperEvent, is(instanceOf(Paper.DraftedEvent.class)));
        Paper.DraftedEvent privateDraftedEvent = (Paper.DraftedEvent) privateDomainPaperEvent;
        assertThat(privateDraftedEvent.getId(), is(equalTo("private id")));
        assertThat(privateDraftedEvent.isPaperPrivate(), is(equalTo(true)));
        assertThat(privateDraftedEvent.isPaperIrrelevant(), is(equalTo(true)));
        assertThat(privateDraftedEvent.isPaperRestricted(), is(equalTo(true)));
        assertThat(privateDraftedEvent.isPaperTolerated(), is(equalTo(true)));
        assertThat(privateDraftedEvent.getTitle(), is(equalTo("abcdef")));
        assertThat(privateDraftedEvent.getAuthorIds(), is(IsArrayContainingInOrder.arrayContaining("1003", "1004")));

    }

    @Test
    public void shouldMapTitleChangedEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        ObjectNode data = jsonNodeFactory.objectNode();
        data.put("title", "the title");
        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "TITLE CHANGED", data, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.TitleChangedEvent.class)));
        Paper.TitleChangedEvent titleChangedEvent = (Paper.TitleChangedEvent) domainPaperEvent;
        assertThat(titleChangedEvent.getId(), is(equalTo("the id")));
        assertThat(titleChangedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(titleChangedEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(titleChangedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
        assertThat(titleChangedEvent.getTitle(), is(equalTo("the title")));
    }

    @Test
    public void shouldMapKeywordsChangedEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        ObjectNode data = jsonNodeFactory.objectNode();
        data.put("keywords", "some, keywords");
        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "KEYWORDS CHANGED", data, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.KeywordsChangedEvent.class)));
        Paper.KeywordsChangedEvent keywordsChangedEvent = (Paper.KeywordsChangedEvent) domainPaperEvent;
        assertThat(keywordsChangedEvent.getId(), is(equalTo("the id")));
        assertThat(keywordsChangedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(keywordsChangedEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(keywordsChangedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
        assertThat(keywordsChangedEvent.getKeywords(), is(equalTo("some, keywords")));
    }

    @Test
    public void shouldMapAuthorChangedEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        ObjectNode data = jsonNodeFactory.objectNode();
        data.set("authorIds", jsonNodeFactory.arrayNode().add(5).add(2).add(9));
        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "AUTHOR CHANGED", data, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.AuthorChangedEvent.class)));
        Paper.AuthorChangedEvent authorChangedEvent = (Paper.AuthorChangedEvent) domainPaperEvent;
        assertThat(authorChangedEvent.getId(), is(equalTo("the id")));
        assertThat(authorChangedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(authorChangedEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(authorChangedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
        assertThat(authorChangedEvent.getAuthorIds(), is(equalTo(new String[]{"5", "2", "9"})));
    }

    @Test
    public void shouldMapMadePrivateEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "MADE PRIVATE", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.MadePrivateEvent.class)));
        Paper.MadePrivateEvent madePrivateEvent = (Paper.MadePrivateEvent) domainPaperEvent;
        assertThat(madePrivateEvent.getId(), is(equalTo("the id")));
        assertThat(madePrivateEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(madePrivateEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(madePrivateEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }

    @Test
    public void shouldMapMadePublicEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "MADE PUBLIC", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.MadePublicEvent.class)));
        Paper.MadePublicEvent madePublicEvent = (Paper.MadePublicEvent) domainPaperEvent;
        assertThat(madePublicEvent.getId(), is(equalTo("the id")));
        assertThat(madePublicEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(madePublicEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(madePublicEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }

    @Test
    public void shouldMapConsideredIrrelevantEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "CONSIDERED IRRELEVANT", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.ConsideredIrrelevantEvent.class)));
        Paper.ConsideredIrrelevantEvent consideredIrrelevant = (Paper.ConsideredIrrelevantEvent) domainPaperEvent;
        assertThat(consideredIrrelevant.getId(), is(equalTo("the id")));
        assertThat(consideredIrrelevant.getEntityId(), is(equalTo("the entity id")));
        assertThat(consideredIrrelevant.getEntityVersion(), is(equalTo(2)));
        assertThat(consideredIrrelevant.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }

    @Test
    public void shouldMapConsideredRelevantEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "CONSIDERED RELEVANT", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.ConsideredRelevantEvent.class)));
        Paper.ConsideredRelevantEvent consideredRelevant = (Paper.ConsideredRelevantEvent) domainPaperEvent;
        assertThat(consideredRelevant.getId(), is(equalTo("the id")));
        assertThat(consideredRelevant.getEntityId(), is(equalTo("the entity id")));
        assertThat(consideredRelevant.getEntityVersion(), is(equalTo(2)));
        assertThat(consideredRelevant.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }

    @Test
    public void shouldMapPaperMarkedRestrictedEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "RESTRICTED", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.RestrictedEvent.class)));
        Paper.RestrictedEvent markedRestricted = (Paper.RestrictedEvent) domainPaperEvent;
        assertThat(markedRestricted.getId(), is(equalTo("the id")));
        assertThat(markedRestricted.getEntityId(), is(equalTo("the entity id")));
        assertThat(markedRestricted.getEntityVersion(), is(equalTo(2)));
        assertThat(markedRestricted.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }

    @Test
    public void shouldMapPaperMarkedUnrestrictedEvents() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "UNRESTRICTED", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.UnrestrictedEvent.class)));
        Paper.UnrestrictedEvent markedUnrestricted = (Paper.UnrestrictedEvent) domainPaperEvent;
        assertThat(markedUnrestricted.getId(), is(equalTo("the id")));
        assertThat(markedUnrestricted.getEntityId(), is(equalTo("the entity id")));
        assertThat(markedUnrestricted.getEntityVersion(), is(equalTo(2)));
        assertThat(markedUnrestricted.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }

    @Test
    public void shouldMapSubmissionStageChangedEvents() {
        // Given
        ObjectNode data = jsonNodeFactory.objectNode();
        data.put("submissionStage", "UNDER REVIEW");
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "SUBMISSION STAGE CHANGED", data, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.SubmissionStageChangedEvent.class)));
        Paper.SubmissionStageChangedEvent submissionStageChangedEvent = (Paper.SubmissionStageChangedEvent) domainPaperEvent;
        assertThat(submissionStageChangedEvent.getId(), is(equalTo("the id")));
        assertThat(submissionStageChangedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(submissionStageChangedEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(submissionStageChangedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
        assertThat(submissionStageChangedEvent.getSubmissionStage(), is(equalTo(UNDER_REVIEW)));
    }

    @Test
    public void shouldMapDeletedEventsAsSubmissionStageChangedToDeletedEvents() {
        // DELETED events are intentionally mapped to SUBMISSION STAGE CHANGED to DELETED events
        // We have not yet implemented the logic to handle DELETEs in the Papers database
        // and Papers rather than PaperUpdates are streamed through Kinesis

        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "DELETED", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.SubmissionStageChangedEvent.class)));
        Paper.SubmissionStageChangedEvent submissionStageChangedEvent = (Paper.SubmissionStageChangedEvent) domainPaperEvent;
        assertThat(submissionStageChangedEvent.getId(), is(equalTo("the id")));
        assertThat(submissionStageChangedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(submissionStageChangedEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(submissionStageChangedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
        assertThat(submissionStageChangedEvent.getSubmissionStage(), is(equalTo(DELETED)));
    }

    @Test
    public void shouldMapEventsWithUnrecognisedTypesToUnrecognisedEvent() {
        // Given
        EventFactory eventFactory = new EventFactory();

        Event oldPlatformPaperEvent = new Event("the id", "the entity id", "SOME UNRECOGNISED TIME", null, 2, "2018-02-23T13:29:47.914768000Z");

        // When
        Paper.Event domainPaperEvent = eventFactory.map(oldPlatformPaperEvent);

        // Then
        assertThat(domainPaperEvent, is(instanceOf(Paper.UnrecognisedEvent.class)));
        Paper.UnrecognisedEvent unrecognisedEvent = (Paper.UnrecognisedEvent) domainPaperEvent;
        assertThat(unrecognisedEvent.getId(), is(equalTo("the id")));
        assertThat(unrecognisedEvent.getEntityId(), is(equalTo("the entity id")));
        assertThat(unrecognisedEvent.getEntityVersion(), is(equalTo(2)));
        assertThat(unrecognisedEvent.getEntityTimestamp(), is(equalTo(new DateTime(2018, 2, 23, 13, 29, 47, 914, DateTimeZone.UTC))));
    }
}