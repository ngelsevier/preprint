package com.ssrn.papers.replicator;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.PaperNotFoundException;
import com.ssrn.papers.domain.PaperRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.ssrn.papers.domain.SubmissionStage.IN_DRAFT;
import static com.ssrn.papers.domain.SubmissionStage.SUBMITTED;
import static com.ssrn.papers.shared.test_support.event.EventBuilder.*;
import static com.ssrn.papers.shared.test_support.event.PaperBuilder.aPaper;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class PaperEventsReplicatorTest {

    @SuppressWarnings("unchecked")
    private static final FeedJobCheckpointer<Paper.Event> ANY_EVENT_FEED_JOB_CHECKPOINTER = (FeedJobCheckpointer<Paper.Event>) mock(FeedJobCheckpointer.class);

    @Test
    public void shouldSaveANewPaperWhenHandlingDraftedEvent() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        Paper.DraftedEvent draftedEvent = aDraftedEvent()
                .withPrivate(true)
                .withIrrelevant(true)
                .withRestricted(true)
                .withTolerated(true)
                .withTitle("some title")
                .withAuthorIds(101, 102)
                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                .build();

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(draftedEvent));

        when(paperRepository.hasPaper("123456789")).thenReturn(false);

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository).save(paperArgumentCaptor.capture());
        Paper paperSavedToRepository = paperArgumentCaptor.getValue();
        assertThat(paperSavedToRepository.getId(), is(equalTo("123456789")));
        assertThat(paperSavedToRepository.getTitle(), is(equalTo("some title")));
        assertThat(paperSavedToRepository.getVersion(), is(equalTo(1)));
        assertThat(paperSavedToRepository.getAuthorIds(), is(arrayContaining("101", "102")));
        assertThat(paperSavedToRepository.isPaperPrivate(), is(equalTo(true)));
        assertThat(paperSavedToRepository.isPaperIrrelevant(), is(equalTo(true)));
        assertThat(paperSavedToRepository.isPaperRestricted(), is(equalTo(true)));
        assertThat(paperSavedToRepository.getSubmissionStage(), is(equalTo(IN_DRAFT)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenHandlingAnyEventOtherThanDraftedEvent() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aTitleChangedEvent().withTitle("the title")
                                .withStandardEventProperties(x -> x
                                        .withEntityId("123456789")
                                        .withEntityVersion(2))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.getTitle(), is(equalTo("the title")));
        assertThat(savedPaper.isPaperPrivate(), is(equalTo(Boolean.FALSE)));
        assertThat(savedPaper.isPaperIrrelevant(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenHandlingSubmissionStageChangedEvent() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aSubmissionStageChangedEvent()
                                .withSubmissionStage(SUBMITTED)
                                .withStandardEventProperties(x -> x
                                        .withEntityId("123456789")
                                        .withEntityVersion(2))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.getSubmissionStage(), is(equalTo(SUBMITTED)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenPaperIsMadePrivate() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aMadePrivateEvent()
                                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.isPaperPrivate(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenPaperIsConsideredIrrelevant() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aConsideredIrrelevantEvent()
                                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.isPaperIrrelevant(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenPaperIsConsideredRelevant() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aConsideredRelevantEvent()
                                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.isPaperIrrelevant(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenPaperIsMadeRestricted() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aRestrictedEvent()
                                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.isPaperRestricted(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenPaperIsMadeUnrestricted() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anUnrestrictedEvent()
                                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.isPaperRestricted(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldUpdateExistingPaperWhenPaperKeywordsAreChanged() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aKeywordsChangedEvent()
                                .withKeywords("any string")
                                .withStandardEventProperties(x -> x.withEntityId("123456789"))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        Paper savedPaper = paperArgumentCaptor.getValue();
        assertThat(savedPaper.getId(), is(equalTo("123456789")));
        assertThat(savedPaper.getKeywords(), is(equalTo("any string")));
    }

    @Test
    public void shouldNotUpdateExistingPaperWhenHandlingAnEventThatDoesNotIncreaseThePaperVersion() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aTitleChangedEvent()
                                .withTitle("a previous title")
                                .withStandardEventProperties(x -> x
                                        .withEntityId("123456789")
                                        .withEntityVersion(2))
                                .build(),
                        aTitleChangedEvent()
                                .withTitle("the title")
                                .withStandardEventProperties(x -> x
                                        .withEntityId("123456789")
                                        .withEntityVersion(3))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("the title").withVersion(3).build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        verify(paperRepository, never()).save(any(Paper.class));
    }

    @Test
    public void shouldNotUpdateExistingPaperWhenHandlingAnEventThatDoesNotConsecutivelyIncreaseThePaperVersion() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aTitleChangedEvent()
                                .withTitle("a new title")
                                .withStandardEventProperties(x -> x
                                        .withEntityId("123456789")
                                        .withEntityVersion(5))
                                .build()
                ));

        when(paperRepository.getById("123456789"))
                .thenReturn(aPaper().withId("123456789").withTitle("the title").withVersion(3).build());

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        verify(paperRepository, never()).save(any(Paper.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotSuppressExceptionsThrownWhenApplyingPaperEventsUnlessDueToEventThatCannotBeApplied() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(aMadePrivateEvent().build()));

        when(paperRepository.getById(any()))
                .thenReturn(new PaperThatOnEventApplicationThrows(new RuntimeException()));

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then throws exception
    }

    @Test
    public void shouldSkipProcessingAnyEventOtherThanDraftedIfPaperDoesNotExist() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aMadePrivateEvent().withStandardEventProperties(x -> x.withEntityId("1")).build(),
                        aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withEntityId("2")).build()
                ));

        when(paperRepository.getById(any()))
                .thenThrow(new PaperNotFoundException("ANY ID"));

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        assertThat(paperArgumentCaptor.getValue().getId(), is(equalTo("2")));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotSuppressExceptionsThrownWhenRetrievingPaperFromRepositoryUnlessDueToNotFoundPaper() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(aMadePrivateEvent().build()));

        when(paperRepository.getById(any()))
                .thenThrow(new RuntimeException(""));

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then expect exception
    }

    @Test
    public void shouldIgnoreDraftedEventWhenEntityAlreadyExists() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withEntityId("123456789")).build()));

        when(paperRepository.hasPaper("123456789")).thenReturn(true);

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        verify(paperRepository, never()).save(any(Paper.class));
    }

    @Test
    public void shouldHandleAllEvents() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(oldPlatformPaperEventsStreamSource, paperRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withEntityId("1")).build(),
                        aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withEntityId("2")).build(),
                        aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withEntityId("3")).build())
                );

        when(paperRepository.hasPaper(any())).thenReturn(false);

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(3)).save(paperArgumentCaptor.capture());

        Paper firstPaperSavedToRepository = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstPaperSavedToRepository.getId(), is(equalTo("1")));

        Paper secondPaperSavedToRepository = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondPaperSavedToRepository.getId(), is(equalTo("2")));

        Paper thirdPaperSavedToRepository = paperArgumentCaptor.getAllValues().get(2);
        assertThat(thirdPaperSavedToRepository.getId(), is(equalTo("3")));
    }

    @Test
    public void shouldResumeProcessingEventsFromWhereLastJobLeftOff() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(
                oldPlatformPaperEventsStreamSource, paperRepository, new InMemoryEventFeedJobCheckpointer());

        Paper.Event firstEvent = aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withId("first-event-id").withEntityId("1")).build();

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent));

        when(paperRepository.hasPaper(any())).thenReturn(false);

        Paper.Event secondEvent = aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withEntityId("2")).build();
        when(oldPlatformPaperEventsStreamSource.getEventsStreamStartingAfter("first-event-id"))
                .thenReturn(Stream.of(secondEvent));

        paperEventsReplicator.replicateNewEvents();

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(2)).save(paperArgumentCaptor.capture());

        Paper firstSavedPaper = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedPaper.getId(), is(equalTo("1")));

        Paper secondSavedPaper = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondSavedPaper.getId(), is(equalTo("2")));
    }

    @Test
    public void shouldResumeProcessingDraftedEventsAfterMostRecentlyProcessedEventInStream() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(
                oldPlatformPaperEventsStreamSource, paperRepository, new InMemoryEventFeedJobCheckpointer());

        Paper.Event firstEvent = aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withId("first-event-id").withEntityId("1")).build();

        doThrow(new RuntimeException("Failed to save paper"))
                .doNothing()
                .when(paperRepository).save(any(Paper.class));

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent))
                .thenReturn(Stream.of(firstEvent));

        when(paperRepository.hasPaper(any())).thenReturn(false);

        try {
            paperEventsReplicator.replicateNewEvents();
        } catch (RuntimeException ignored) {
        }

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(2)).save(paperArgumentCaptor.capture());

        Paper firstSavedPaper = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedPaper.getId(), is(equalTo("1")));

        Paper secondSavedPaper = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondSavedPaper.getId(), is(equalTo("1")));
    }

    @Test
    public void shouldCheckpointDraftedEventWhenPaperAlreadyExists() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        FeedJobCheckpointer<Paper.Event> feedJobCheckpointer = (FeedJobCheckpointer<Paper.Event>) mock(FeedJobCheckpointer.class);

        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(
                oldPlatformPaperEventsStreamSource, paperRepository, feedJobCheckpointer);

        Paper.Event firstEvent = aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withId("first-event-id").withEntityId("1")).build();

        doThrow(new RuntimeException("Failed to save paper"))
                .when(paperRepository).save(any(Paper.class));

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent));

        when(paperRepository.hasPaper(any())).thenReturn(true);


        // When
        try {
            paperEventsReplicator.replicateNewEvents();
        } catch (RuntimeException ignored) {
        }

        // Then
        verify(feedJobCheckpointer, times(1)).checkpoint(firstEvent);
    }

    @Test
    public void shouldResumeProcessingNonDraftedEventsAfterMostRecentlyProcessedEventInStream() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(
                oldPlatformPaperEventsStreamSource, paperRepository, new InMemoryEventFeedJobCheckpointer());

        Paper.Event firstEvent = aMadePrivateEvent().withStandardEventProperties(x -> x.withId("first-event-id").withEntityId("1")).build();

        doThrow(new RuntimeException("Failed to save paper"))
                .doNothing()
                .when(paperRepository).save(any(Paper.class));

        when(paperRepository.getById("1"))
                .thenReturn(aPaper().withId("1").build())
                .thenReturn(aPaper().withId("1").build());

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent))
                .thenReturn(Stream.of(firstEvent));

        try {
            paperEventsReplicator.replicateNewEvents();
        } catch (RuntimeException ignored) {
        }

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(2)).save(paperArgumentCaptor.capture());

        Paper firstSavedPaper = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedPaper.getId(), is(equalTo("1")));

        Paper secondSavedPaper = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondSavedPaper.getId(), is(equalTo("1")));
    }

    @Test
    public void shouldNotReprocessEntireEventFeedOnSubsequentRunIfFinalEventInFeedHasBeenProcessed() {
        // Given
        OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource = mock(OldPlatformPaperEventsStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);
        PaperEventsReplicator paperEventsReplicator = new PaperEventsReplicator(
                oldPlatformPaperEventsStreamSource, paperRepository, new InMemoryEventFeedJobCheckpointer());

        Paper.Event firstEvent = aDraftedEvent().withPrivate(false).withIrrelevant(false).withRestricted(false).withTolerated(false).withTitle("123456").withAuthorIds(101, 102).withStandardEventProperties(x -> x.withId("first-event-id").withEntityId("1")).build();

        when(oldPlatformPaperEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent));

        when(paperRepository.hasPaper(any())).thenReturn(false);

        when(oldPlatformPaperEventsStreamSource.getEventsStreamStartingAfter("first-event-id"))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty());

        paperEventsReplicator.replicateNewEvents();
        paperEventsReplicator.replicateNewEvents();

        // When
        paperEventsReplicator.replicateNewEvents();

        // Then
        ArgumentCaptor<Paper> paperArgumentCaptor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());

        Paper firstSavedPaper = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedPaper.getId(), is(equalTo("1")));
    }

    private static class InMemoryEventFeedJobCheckpointer implements FeedJobCheckpointer<Paper.Event> {

        private Optional<String> lastCheckpoint = Optional.empty();

        @Override
        public void checkpoint(Paper.Event event) {
            lastCheckpoint = Optional.of(event.getId());
        }

        @Override
        public Optional<String> getLastCheckpoint() {
            return lastCheckpoint;
        }
    }

    private class PaperThatOnEventApplicationThrows extends Paper {

        private final RuntimeException exceptionThrownOnEventApplication;

        PaperThatOnEventApplicationThrows(RuntimeException exceptionThrownOnEventApplication) {
            super(null, 0, null, null, null, false, false, false, IN_DRAFT);
            this.exceptionThrownOnEventApplication = exceptionThrownOnEventApplication;
        }

        @Override
        protected <TEvent extends Event> void apply(TEvent event, Consumer<TEvent> eventProjector) {
            throw exceptionThrownOnEventApplication;
        }
    }
}
