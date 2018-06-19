package com.ssrn.authors.replicator;

import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorNotFoundException;
import com.ssrn.authors.domain.AuthorRepository;
import com.ssrn.authors.domain.Event;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.stream.Stream;

import static com.ssrn.authors.shared.test_support.entity.AuthorBuilder.anAuthor;
import static com.ssrn.authors.shared.test_support.event.EventBuilder.anAuthorRegisteredEvent;
import static com.ssrn.authors.shared.test_support.event.EventBuilder.anEvent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class AuthorEventsProjectorTest {

    @SuppressWarnings("unchecked")
    private static final FeedJobCheckpointer<Event> ANY_EVENT_FEED_JOB_CHECKPOINTER = (FeedJobCheckpointer<Event>) mock(FeedJobCheckpointer.class);

    @Test
    public void shouldSaveANewAuthorWhenHandlingARegisteredEvent() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(anEvent()
                        .withEntityVersion(25)
                        .withType("REGISTERED")
                        .withData(new JSONObject().put("name", "Author Name"))
                        .withEntityId("123456789")
                        .build()));
        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(authorArgumentCaptor.capture());
        Author authorSavedToRepository = authorArgumentCaptor.getValue();
        assertThat(authorSavedToRepository.getId(), is(equalTo("123456789")));
        assertThat(authorSavedToRepository.getName(), is(equalTo("Author Name")));
        assertThat(authorSavedToRepository.getVersion(), is(equalTo(25)));
        assertThat(authorSavedToRepository.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldRemoveAnAuthorWhenHandlingAnUnregisteredEvent() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(anEvent()
                        .withEntityVersion(25)
                        .withType("UNREGISTERED")
                        .withData(new JSONObject().put("name", "Author Name"))
                        .withEntityId("123456789")
                        .build()));

        when(authorRepository.getById("123456789"))
                .thenReturn(anAuthor().withId("123456789").withVersion(24).withName("Author Name").withRemoval(false).build());

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(authorArgumentCaptor.capture());
        Author authorSavedToRepository = authorArgumentCaptor.getValue();
        assertThat(authorSavedToRepository.getId(), is(equalTo("123456789")));
        assertThat(authorSavedToRepository.getName(), is(equalTo("Author Name")));
        assertThat(authorSavedToRepository.getVersion(), is(equalTo(25)));
        assertThat(authorSavedToRepository.isRemoved(), is(equalTo(true)));
    }

    @Test
    public void shouldUpdateExistingAuthorWhenHandlingAnyEventOtherThanRegisteredEvent() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anEvent().withType("NAME CHANGED")
                                .withEntityId("123456789")
                                .withEntityVersion(5)
                                .withData(new JSONObject().put("name", "Updated Name"))
                                .build()
                ));

        when(authorRepository.getById("123456789"))
                .thenReturn(anAuthor().withId("123456789").withVersion(4).withName("Initial Name").withRemoval(false).build());

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(1)).save(authorArgumentCaptor.capture());
        Author savedAuthor = authorArgumentCaptor.getValue();
        assertThat(savedAuthor.getId(), is(equalTo("123456789")));
        assertThat(savedAuthor.getName(), is(equalTo("Updated Name")));
        assertThat(savedAuthor.getVersion(), is(equalTo(5)));
        assertThat(savedAuthor.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldNotUpdateExistingAuthorWhenHandlingAnEventThatDoesNotIncreaseTheAuthorVersion() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anEvent().withType("NAME CHANGED")
                                .withEntityId("123456789")
                                .withEntityVersion(2)
                                .withData(new JSONObject().put("name", "a previous name"))
                                .build(),
                        anEvent().withType("NAME CHANGED")
                                .withEntityId("123456789")
                                .withEntityVersion(3)
                                .withData(new JSONObject().put("name", "the name"))
                                .build()
                ));

        when(authorRepository.getById("123456789"))
                .thenReturn(anAuthor().withId("123456789").withName("the name").withVersion(3).build());

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    public void shouldNotUpdateExistingAuthorWhenHandlingAnEventThatDoesNotConsecutivelyIncreaseTheAuthorVersion() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anEvent().withType("NAME CHANGED")
                                .withEntityId("123456789")
                                .withEntityVersion(5)
                                .withData(new JSONObject().put("name", "a new name"))
                                .build()
                ));

        when(authorRepository.getById("123456789"))
                .thenReturn(anAuthor().withId("123456789").withName("the name").withVersion(3).build());

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotSuppressExceptionsThrownWhenApplyingAuthorEventsUnlessDueToEventThatCannotBeApplied() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(anEvent().withEntityVersion(2).build()));

        when(authorRepository.getById(any()))
                .thenReturn(new AuthorThatOnEventApplicationThrows(new RuntimeException()));

        // When
        authorEventsProjector.applyNewEvents();

        // Then throws exception
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotSuppressExceptionsThrownWhenRetrievingAuthorFromRepositoryUnlessDueToNotFoundAuthor() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(anEvent().withEntityVersion(2).build()));

        when(authorRepository.getById(any()))
                .thenThrow(new RuntimeException(""));

        // When
        authorEventsProjector.applyNewEvents();

        // Then expect exception
    }

    @Test
    public void shouldSkipProcessingAnyEventOtherThanRegisteredForAnAuthorThatDoesNotExist() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anEvent().withEntityId("1").withType("NAME CHANGED").withData(new JSONObject().put("name", "Author 1")).build(),
                        anEvent().withEntityId("2").withType("REGISTERED").withData(new JSONObject().put("name", "Author 2")).build()
                ));

        when(authorRepository.getById(any()))
                .thenThrow(new AuthorNotFoundException());

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(1)).save(authorArgumentCaptor.capture());
        assertThat(authorArgumentCaptor.getValue().getId(), is(equalTo("2")));
    }

    @Test
    public void shouldUpdateExistingAuthorInResponseToRegisteredEvent() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anEvent().withType("REGISTERED")
                                .withEntityId("123456789")
                                .withEntityVersion(3)
                                .withData(new JSONObject().put("name", "Updated Name"))
                                .build()
                ));

        when(authorRepository.getById("123456789"))
                .thenReturn(anAuthor().withName("Initial Name").withId("123456789").withVersion(2).build());

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(1)).save(authorArgumentCaptor.capture());
        assertThat(authorArgumentCaptor.getValue().getId(), is(equalTo("123456789")));
        assertThat(authorArgumentCaptor.getValue().getName(), is(equalTo("Updated Name")));
        assertThat(authorArgumentCaptor.getValue().getVersion(), is(equalTo(3)));
    }

    @Test
    public void shouldApplyAllEventsInBatch() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(oldPlatformAuthorEventsStreamSource, authorRepository, ANY_EVENT_FEED_JOB_CHECKPOINTER);

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(
                        anAuthorRegisteredEvent().withEntityId("1").build(),
                        anAuthorRegisteredEvent().withEntityId("2").build(),
                        anAuthorRegisteredEvent().withEntityId("3").build())
                );

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(3)).save(authorArgumentCaptor.capture());

        Author firstAuthorSavedToRepository = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstAuthorSavedToRepository.getId(), is(equalTo("1")));

        Author secondAuthorSavedToRepository = authorArgumentCaptor.getAllValues().get(1);
        assertThat(secondAuthorSavedToRepository.getId(), is(equalTo("2")));

        Author thirdAuthorSavedToRepository = authorArgumentCaptor.getAllValues().get(2);
        assertThat(thirdAuthorSavedToRepository.getId(), is(equalTo("3")));
    }

    @Test
    public void shouldResumeProcessingEventsFromWhereLastJobLeftOff() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);

        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(
                oldPlatformAuthorEventsStreamSource, authorRepository, new InMemoryEventFeedJobCheckpointer());

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(anAuthorRegisteredEvent().withId("first-event-id").withEntityId("1").build()));

        when(oldPlatformAuthorEventsStreamSource.getEventsStreamStartingAfter("first-event-id"))
                .thenReturn(Stream.of(anAuthorRegisteredEvent().withEntityId("2").build()));

        authorEventsProjector.applyNewEvents();

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(2)).save(authorArgumentCaptor.capture());

        Author firstSavedAuthor = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedAuthor.getId(), is(equalTo("1")));

        Author secondSavedAuthor = authorArgumentCaptor.getAllValues().get(1);
        assertThat(secondSavedAuthor.getId(), is(equalTo("2")));
    }

    @Test
    public void shouldResumeProcessingEventsByRetryingFailedEventWhenPreviousBatchAttemptThrewException() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);

        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(
                oldPlatformAuthorEventsStreamSource, authorRepository, new InMemoryEventFeedJobCheckpointer());

        Event firstEvent = anAuthorRegisteredEvent().withEntityId("1").build();

        doThrow(new RuntimeException("Failed to save author"))
                .doNothing()
                .when(authorRepository).save(any(Author.class));

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent))
                .thenReturn(Stream.of(firstEvent));

        try {
            authorEventsProjector.applyNewEvents();
        } catch (RuntimeException ignored) {
        }

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(2)).save(authorArgumentCaptor.capture());

        Author firstSavedAuthor = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedAuthor.getId(), is(equalTo("1")));

        Author secondSavedAuthor = authorArgumentCaptor.getAllValues().get(1);
        assertThat(secondSavedAuthor.getId(), is(equalTo("1")));
    }

    @Test
    public void shouldNotReprocessEntireEventFeedOnSubsequentRunIfFinalEventInFeedHasBeenProcessed() {
        // Given
        OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource = mock(OldPlatformAuthorEventsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);
        AuthorEventsProjector authorEventsProjector = new AuthorEventsProjector(
                oldPlatformAuthorEventsStreamSource, authorRepository, new InMemoryEventFeedJobCheckpointer());

        Event firstEvent = anAuthorRegisteredEvent().withId("first-event-id").withEntityId("1").build();

        when(oldPlatformAuthorEventsStreamSource.getEventsStream())
                .thenReturn(Stream.of(firstEvent));

        when(oldPlatformAuthorEventsStreamSource.getEventsStreamStartingAfter("first-event-id"))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty());

        authorEventsProjector.applyNewEvents();
        authorEventsProjector.applyNewEvents();

        // When
        authorEventsProjector.applyNewEvents();

        // Then
        ArgumentCaptor<Author> authorArgumentCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository, times(1)).save(authorArgumentCaptor.capture());

        Author firstSavedAuthor = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstSavedAuthor.getId(), is(equalTo("1")));
    }

    private static class InMemoryEventFeedJobCheckpointer implements FeedJobCheckpointer<Event> {

        private Optional<String> lastCheckpoint = Optional.empty();

        @Override
        public void checkpoint(Event event) {
            lastCheckpoint = Optional.of(event.getId());
        }

        @Override
        public Optional<String> getLastCheckpoint() {
            return lastCheckpoint;
        }
    }

    private class AuthorThatOnEventApplicationThrows extends Author {

        private final RuntimeException exceptionThrownOnEventApplication;

        AuthorThatOnEventApplicationThrows(RuntimeException exceptionThrownOnEventApplication) {
            super(null, 0, null, false);
            this.exceptionThrownOnEventApplication = exceptionThrownOnEventApplication;
        }

        @Override
        public void apply(Event event) {
            throw exceptionThrownOnEventApplication;
        }
    }
}
