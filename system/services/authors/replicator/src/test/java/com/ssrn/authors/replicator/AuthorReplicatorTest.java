package com.ssrn.authors.replicator;

import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ssrn.authors.shared.test_support.entity.AuthorBuilder.anAuthor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthorReplicatorTest {

    @SuppressWarnings("unchecked")
    private static final FeedJobCheckpointer<Author> ANY_AUTHOR_CHECKPOINTER = (FeedJobCheckpointer<Author>) mock(FeedJobCheckpointer.class);

    @Test
    public void shouldSaveAuthorToAuthorRepositoryForEachAuthorInStream() {

        // Given
        OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource = mock(OldPlatformAuthorsStreamSource.class);

        AuthorRepository authorRepository = mock(AuthorRepository.class);

        AuthorReplicator authorReplicator = new AuthorReplicator(oldPlatformAuthorsStreamSource, authorRepository, ANY_AUTHOR_CHECKPOINTER);

        Author[] authors = {
                anAuthor().withId("101").build(),
                anAuthor().withId("102").build(),
                anAuthor().withId("103").build(),
        };

        when(oldPlatformAuthorsStreamSource.getAuthorsStream())
                .thenReturn(Stream.of(authors));

        // When
        authorReplicator.replicateAuthors(1000, 100);

        // Then
        ArgumentCaptor<List<Author>> authorArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRepository, times(1)).save(authorArgumentCaptor.capture());
        List<Author> savedAuthors = authorArgumentCaptor.getValue();
        assertThat(savedAuthors, hasSize(3));
        assertThat(savedAuthors.get(0).getId(), is(equalTo("101")));
        assertThat(savedAuthors.get(1).getId(), is(equalTo("102")));
        assertThat(savedAuthors.get(2).getId(), is(equalTo("103")));
    }

    @Test
    public void shouldNotSaveMoreThanSpecifiedNumberOfAuthors() {
        // Given
        OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource = mock(OldPlatformAuthorsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);

        AuthorReplicator authorReplicator = new AuthorReplicator(oldPlatformAuthorsStreamSource, authorRepository, ANY_AUTHOR_CHECKPOINTER);

        when(oldPlatformAuthorsStreamSource.getAuthorsStream())
                .thenReturn(Stream.of(anAuthor().withId("101").build(), anAuthor().withId("102").build(), anAuthor().withId("103").build()));

        // When
        authorReplicator.replicateAuthors(2, 100);

        // Then
        ArgumentCaptor<List<Author>> authorArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRepository, times(1)).save(authorArgumentCaptor.capture());
        List<Author> savedAuthors = authorArgumentCaptor.getValue();
        assertThat(savedAuthors, hasSize(2));
        assertThat(savedAuthors.get(0).getId(), is(equalTo("101")));
        assertThat(savedAuthors.get(1).getId(), is(equalTo("102")));
    }

    @Test
    public void shouldNotSaveMoreThanSpecifiedNumberOfAuthorsPerRepositoryRequest() {
        // Given
        OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource = mock(OldPlatformAuthorsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);

        AuthorReplicator authorReplicator = new AuthorReplicator(oldPlatformAuthorsStreamSource, authorRepository, ANY_AUTHOR_CHECKPOINTER);

        when(oldPlatformAuthorsStreamSource.getAuthorsStream())
                .thenReturn(Stream.of(anAuthor().withId("101").build(), anAuthor().withId("102").build(), anAuthor().withId("103").build()));

        // When
        authorReplicator.replicateAuthors(1000, 2);

        // Then
        ArgumentCaptor<List<Author>> authorArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRepository, times(2)).save(authorArgumentCaptor.capture());
        List<Author> firstBatchOfSavedAuthor = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstBatchOfSavedAuthor, hasSize(2));
        assertThat(firstBatchOfSavedAuthor.get(0).getId(), is(equalTo("101")));
        assertThat(firstBatchOfSavedAuthor.get(1).getId(), is(equalTo("102")));

        List<Author> secondBatchOfSavedAuthor = authorArgumentCaptor.getAllValues().get(1);
        assertThat(secondBatchOfSavedAuthor, hasSize(1));
        assertThat(secondBatchOfSavedAuthor.get(0).getId(), is(equalTo("103")));
    }

    @Test
    public void shouldResumeProcessingAuthorsFromWhereLastJobLeftOff() {
        // Given
        OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource = mock(OldPlatformAuthorsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);

        InMemoryEntityFeedJobCheckpointer authorCheckpointer = new InMemoryEntityFeedJobCheckpointer();

        AuthorReplicator authorReplicator = new AuthorReplicator(oldPlatformAuthorsStreamSource, authorRepository, authorCheckpointer);

        when(oldPlatformAuthorsStreamSource.getAuthorsStream())
                .thenReturn(Stream.of(anAuthor().withId("100").build(), anAuthor().withId("101").build()));

        Author[] authors = {anAuthor().withId("102").build(), anAuthor().withId("103").build()};
        when(oldPlatformAuthorsStreamSource.getAuthorsStreamAfterId("101"))
                .thenReturn(Stream.of(authors));

        authorReplicator.replicateAuthors(1000, 100);

        // When
        authorReplicator.replicateAuthors(1000, 100);

        // Then
        ArgumentCaptor<List<Author>> authorArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRepository, times(2)).save(authorArgumentCaptor.capture());

        List<Author> firstBatchOfSavedAuthors = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstBatchOfSavedAuthors, hasSize(2));
        assertThat(firstBatchOfSavedAuthors.get(0).getId(), is(equalTo("100")));
        assertThat(firstBatchOfSavedAuthors.get(1).getId(), is(equalTo("101")));

        List<Author> secondBatchOfSavedAuthors = authorArgumentCaptor.getAllValues().get(1);
        assertThat(secondBatchOfSavedAuthors, hasSize(2));
        assertThat(secondBatchOfSavedAuthors.get(0).getId(), is(equalTo("102")));
        assertThat(secondBatchOfSavedAuthors.get(1).getId(), is(equalTo("103")));
    }

    @Test
    public void shouldResumeProcessingAuthorsAfterMostRecentlyProcessedAuthorInStream() {
        // Given
        OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource = mock(OldPlatformAuthorsStreamSource.class);
        AuthorRepository authorRepository = mock(AuthorRepository.class);

        InMemoryEntityFeedJobCheckpointer authorCheckpointer = new InMemoryEntityFeedJobCheckpointer();

        AuthorReplicator authorReplicator = new AuthorReplicator(oldPlatformAuthorsStreamSource, authorRepository, authorCheckpointer);

        Author author101 = anAuthor().withId("101").build();
        Author author102 = anAuthor().withId("102").build();

        when(oldPlatformAuthorsStreamSource.getAuthorsStream())
                .thenReturn(Stream.of(author101))
                .thenReturn(Stream.of(author101, author102));

        doThrow(new RuntimeException("Could not save author"))
                .doNothing()
                .when(authorRepository).save(any(List.class));

        try {
            authorReplicator.replicateAuthors(1000, 100);
        } catch (RuntimeException ignored) {
        }

        // When
        authorReplicator.replicateAuthors(1000, 100);

        // Then
        ArgumentCaptor<List<Author>> authorArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRepository, times(2)).save(authorArgumentCaptor.capture());

        List<Author> firstBatchOfSavedAuthors = authorArgumentCaptor.getAllValues().get(0);
        assertThat(firstBatchOfSavedAuthors, hasSize(1));
        assertThat(firstBatchOfSavedAuthors.get(0).getId(), is(equalTo("101")));

        List<Author> secondBatchOfSavedAuthors = authorArgumentCaptor.getAllValues().get(1);
        assertThat(secondBatchOfSavedAuthors, hasSize(2));
        assertThat(secondBatchOfSavedAuthors.get(0).getId(), is(equalTo("101")));
        assertThat(secondBatchOfSavedAuthors.get(1).getId(), is(equalTo("102")));
    }

    public static class InMemoryEntityFeedJobCheckpointer implements FeedJobCheckpointer<Author> {
        private Optional<String> lastProcessedAuthorId = Optional.empty();

        @Override
        public void checkpoint(Author author) {
            lastProcessedAuthorId = Optional.of(author.getId());
        }

        @Override
        public Optional<String> getLastCheckpoint() {
            return lastProcessedAuthorId;
        }
    }
}
