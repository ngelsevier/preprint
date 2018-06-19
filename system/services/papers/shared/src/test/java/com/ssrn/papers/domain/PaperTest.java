package com.ssrn.papers.domain;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.ssrn.papers.domain.SubmissionStage.SUBMITTED;
import static com.ssrn.papers.shared.test_support.event.EventBuilder.*;
import static com.ssrn.papers.shared.test_support.event.PaperBuilder.aPaper;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PaperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JSONObject paperJson;

    @Before
    public void before() {
        paperJson = new JSONObject()
                .put("id", "id")
                .put("title", "any")
                .put("keywords", "any")
                .put("version", 22)
                .put("authorIds", new String[]{"id"})
                .put("paperPrivate", true)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", SubmissionStage.UNDER_REVIEW.getName());
    }

    @Test
    public void shouldRequireIdWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("id");
    }

    @Test
    public void shouldRequireTitleWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("title");
    }

    @Test
    public void shouldNotRequireKeywordsPropertyInSourceJson() throws IOException {
        // Given
        paperJson.remove("keywords");
        String paperJsonMissingProperty = paperJson.toString();

        ObjectMapper mapper = new ObjectMapper();

        // When
        mapper.readValue(paperJsonMissingProperty, Paper.class);

        // Then no exceptions
    }

    @Test
    public void shouldRequireVersionWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("version");
    }

    @Test
    public void shouldNotRequireAuthorIdsPropertyInSourceJson() throws IOException {
        // Given
        paperJson.remove("authorIds");
        String paperJsonMissingProperty = paperJson.toString();

        ObjectMapper mapper = new ObjectMapper();

        // When
        mapper.readValue(paperJsonMissingProperty, Paper.class);

        // Then no exceptions
    }

    @Test
    public void shouldRequirePaperPrivateFlagWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("paperPrivate");
    }

    @Test
    public void shouldRequirePaperIrrelevantFlagWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("paperIrrelevant");
    }

    @Test
    public void shouldRequirePaperRestrictedFlagWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("paperRestricted");
    }

    @Test
    public void shouldRequireSubmissionStageWhenDeserializingFromJson() {
        shouldRequirePropertyInSourceJson("submissionStage");
    }

    @Test
    public void shouldDeserializeSubmissionStageFromJson() throws IOException {
        objectMapper.readValue("{ \"id\": 1, \"title\": \"any title\", \"version\": 1, \"paperPrivate\": false, \"paperIrrelevant\": false, \"paperRestricted\": false, \"submissionStage\": \"UNDER REVIEW\" }", Paper.class);
    }

    @Test
    public void shouldUpdateTitleWhenTitleChangedEventApplied() {
        // Given
        Paper paper = aPaper().withTitle("initial title").withVersion(2).build();

        Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent()
                .withTitle("new title")
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        titleChangedEvent.applyOn(paper);

        // Then
        assertThat(paper.getTitle(), is(equalTo("new title")));
    }

    @Test
    public void shouldUpdateKeywordsWhenKeywordsChangedEventApplied() {
        // Given
        Paper paper = aPaper().withKeywords("some changes").withVersion(2).build();

        Paper.KeywordsChangedEvent titleChangedEvent = aKeywordsChangedEvent()
                .withKeywords("updated changes")
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        titleChangedEvent.applyOn(paper);

        // Then
        assertThat(paper.getKeywords(), is(equalTo("updated changes")));
    }

    @Test
    public void shouldUpdateAuthorIdsWhenAuthorChangedEventApplied() {
        // Given
        Paper paper = aPaper().withAuthorIds("102").withVersion(2).build();

        Paper.AuthorChangedEvent authorChangedEvent = anAuthorChangedEvent()
                .withAuthorIds(102, 101)
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        authorChangedEvent.applyOn(paper);

        // Then
        assertThat(paper.getAuthorIds(), is(equalTo(new String[]{"102", "101"})));
    }

    @Test
    public void shouldUpdateVersionToEventEntityVersionWhenEventApplied() {
        // Given
        Paper paper = aPaper().withTitle("initial title").withVersion(2).build();

        Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent()
                .withTitle("new title")
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        titleChangedEvent.applyOn(paper);

        // Then
        assertThat(paper.getTitle(), is(equalTo("new title")));
        assertThat(paper.getVersion(), is(equalTo(3)));
    }

    @Test
    public void shouldUpdateVersionToEventEntityVersionEvenWhenUnsupportEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(1).build();

        Paper.UnrecognisedEvent unrecognisedEvent = anUnrecognisedEvent().build();

        // When
        unrecognisedEvent.applyOn(paper);

        // Then
        assertThat(paper.getVersion(), is(equalTo(2)));
    }

    @Test
    public void shouldNotThrowExceptionWhenUnrecognisedEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(1).build();

        Paper.UnrecognisedEvent unrecognisedEvent = anUnrecognisedEvent().build();

        // When
        unrecognisedEvent.applyOn(paper);

        // Then should not throw an exception
    }

    @Test
    public void shouldNotApplyAnEventForAnOlderVersion() {
        // Given
        Paper paper = aPaper().withTitle("new title").withVersion(3).build();

        Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent().withTitle("previous title").build();

        // When
        try {
            titleChangedEvent.applyOn(paper);
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            assertThat(e.getEvent(), is(equalTo(titleChangedEvent)));
            assertThat(e.getCurrentVersion(), is(equalTo(3)));
        }

        // Then
        assertThat(paper.getTitle(), is(equalTo("new title")));
        assertThat(paper.getVersion(), is(equalTo(3)));
    }

    @Test
    public void shouldNotApplyAnEventForTheCurrentVersion() {
        // Given
        Paper paper = aPaper().withTitle("initial title").withVersion(3).build();

        Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent()
                .withTitle("new title")
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        try {
            titleChangedEvent.applyOn(paper);
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            assertThat(e.getEvent(), is(equalTo(titleChangedEvent)));
            assertThat(e.getCurrentVersion(), is(equalTo(3)));
        }

        // Then
        assertThat(paper.getTitle(), is(equalTo("initial title")));
        assertThat(paper.getVersion(), is(equalTo(3)));
    }

    @Test
    public void shouldNotApplyAnEventThatWouldIncreaseTheVersionByMoreThanOne() {
        // Given
        Paper paper = aPaper().withTitle("initial title").withVersion(3).build();

        Paper.TitleChangedEvent titleChangedEvent = aTitleChangedEvent()
                .withTitle("new title")
                .withStandardEventProperties(x -> x.withEntityVersion(5))
                .build();

        // When
        try {
            titleChangedEvent.applyOn(paper);
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            assertThat(e.getEvent(), is(equalTo(titleChangedEvent)));
            assertThat(e.getCurrentVersion(), is(equalTo(3)));
        }

        // Then
        assertThat(paper.getTitle(), is(equalTo("initial title")));
        assertThat(paper.getVersion(), is(equalTo(3)));
    }

    @Test
    public void shouldMarkPaperAsPrivateWhenPaperMadePrivateEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withPaperPrivate(false).build();

        Paper.MadePrivateEvent paperMadePrivateEvent = aMadePrivateEvent()
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        paperMadePrivateEvent.applyOn(paper);

        // Then
        assertThat(paper.isPaperPrivate(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldMarkPaperAsPublicWhenPaperMadePublicEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withPaperPrivate(true).build();

        Paper.MadePublicEvent madePublicEvent = aMadePublicEvent()
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        madePublicEvent.applyOn(paper);

        // Then
        assertThat(paper.isPaperPrivate(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldMarkPaperAsIrrelevantWhenPaperConsideredIrrelevantEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withPaperPrivate(false).withPaperIrrelevant(true).build();

        Paper.ConsideredIrrelevantEvent consideredIrrelevantEvent = aConsideredIrrelevantEvent()
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        consideredIrrelevantEvent.applyOn(paper);

        // Then
        assertThat(paper.isPaperIrrelevant(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldMarkPaperAsRelevantWhenPaperConsideredRelevantEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withPaperPrivate(false).withPaperIrrelevant(false).build();

        Paper.ConsideredRelevantEvent consideredRelevantEvent = aConsideredRelevantEvent()
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        consideredRelevantEvent.applyOn(paper);

        // Then
        assertThat(paper.isPaperIrrelevant(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldMarkPaperAsRestrictedWhenPaperRestrictedEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withPaperPrivate(false).withPaperIrrelevant(false).withPaperRestricted(true).build();

        Paper.RestrictedEvent restrictedEvent = aRestrictedEvent()
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        restrictedEvent.applyOn(paper);

        // Then
        assertThat(paper.isPaperRestricted(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldMarkPaperAsUnrestrictedWhenPaperUnrestrictedEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withPaperPrivate(false).withPaperIrrelevant(false).withPaperRestricted(false).build();

        Paper.UnrestrictedEvent unrestrictedEvent = anUnrestrictedEvent()
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        unrestrictedEvent.applyOn(paper);

        // Then
        assertThat(paper.isPaperRestricted(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldApplySubmissionStageWhenPaperSubmissionStageChangedEventApplied() {
        // Given
        Paper paper = aPaper().withVersion(2).withSubmissionStage(SUBMITTED).build();

        Paper.SubmissionStageChangedEvent submissionStageChangedEvent = aSubmissionStageChangedEvent()
                .withSubmissionStage(SUBMITTED)
                .withStandardEventProperties(x -> x.withEntityVersion(3))
                .build();

        // When
        submissionStageChangedEvent.applyOn(paper);

        // Then
        assertThat(paper.getSubmissionStage(), is(equalTo(SUBMITTED)));
    }

    private void shouldRequirePropertyInSourceJson(String property) {
        // Given
        paperJson.remove(property);

        ObjectMapper mapper = new ObjectMapper();

        // When
        try {
            mapper.readValue(paperJson.toString(), Paper.class);
            fail();
        } catch (Exception e) {
            // Then throws exception
            assertThat(e, instanceOf(JsonMappingException.class));
            assertThat(e.getMessage(), containsString("Missing required creator property '" + property + "'"));
        }
    }

}
