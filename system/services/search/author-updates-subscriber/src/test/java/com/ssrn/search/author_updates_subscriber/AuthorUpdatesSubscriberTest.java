package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.shared.AuthorRegistry;
import com.ssrn.search.shared.Library;
import com.ssrn.search.shared.SearchIndexPaper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.AuthorUpdateBuilder.anAuthorUpdate;
import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static com.ssrn.search.shared.SearchIndexPaperBuilder.aSearchIndexPaper;
import static com.ssrn.search.shared.matchers.mockito.CollectionContainingOnlyMatcher.collectionContainingOnly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AuthorUpdatesSubscriberTest {
    @Test
    public void shouldUpdateAuthorsRegistryAndLibraryWithAuthorsReceivedFromAuthorUpdatesStream() {
        // Given
        SearchIndexPaper initialVersionOfFirstPaper = aSearchIndexPaper()
                .withId("101")
                .withTitle("First Paper")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("1").withNoName()
                )
                .build();

        SearchIndexPaper initialVersionOfSecondPaper = aSearchIndexPaper()
                .withId("102")
                .withTitle("Second Paper")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("2").withName("Original Author 2 Name"),
                        aSearchIndexPaperAuthor().withId("3").withName("Original Author 3 Name")
                )
                .build();

        SearchIndexPaper initialVersionOfThirdPaper = aSearchIndexPaper()
                .withId("103")
                .withTitle("Third Paper")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("3").withName("Original Author 3 Name"),
                        aSearchIndexPaperAuthor().withId("4").withName("Original Author 4 Name")
                )
                .build();

        Library library = mock(Library.class);
        AuthorUpdate.Author firstAuthor = anAuthor().withId("1").withName("Updated Author 1 Name").build();
        AuthorUpdate.Author secondAuthor = anAuthor().withId("2").withName("Original Author 2 Name").build();
        AuthorUpdate.Author thirdAuthor = anAuthor().withId("3").withName("Updated Author 3 Name").build();
        when(library.getPapersWrittenBy(collectionContainingOnly(firstAuthor, secondAuthor, thirdAuthor)))
                .thenReturn(Stream.of(
                        initialVersionOfFirstPaper,
                        initialVersionOfSecondPaper,
                        initialVersionOfThirdPaper
                ));

        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        AuthorUpdatesStream authorUpdatesStream = mock(AuthorUpdatesStream.class);
        AuthorUpdatesSubscriber authorUpdatesSubscriber = new AuthorUpdatesSubscriber(library, authorRegistry);
        authorUpdatesSubscriber.subscribeTo(authorUpdatesStream);

        Consumer<List<AuthorUpdate>> consumerRegisteredOnAuthorUpdatesStream = getAuthorUpdatesConsumerRegisteredOn(authorUpdatesStream);

        List<AuthorUpdate> authorUpdates = asList(
                anAuthorUpdate().withAuthor(firstAuthor).build(),
                anAuthorUpdate().withAuthor(secondAuthor).build(),
                anAuthorUpdate().withAuthor(thirdAuthor).build()
        );

        // When
        consumerRegisteredOnAuthorUpdatesStream.accept(authorUpdates);

        // Then
        ArgumentCaptor<List<AuthorUpdate.Author>> authorRegistryArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRegistry).update(authorRegistryArgumentCaptor.capture());
        List<AuthorUpdate.Author> authorsUpdatedInRegistry = authorRegistryArgumentCaptor.getValue();
        assertThat(authorsUpdatedInRegistry, hasSize(3));
        assertThat(authorsUpdatedInRegistry, containsInAnyOrder(firstAuthor, secondAuthor, thirdAuthor));

        ArgumentCaptor<List<SearchIndexPaper>> libraryArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library, times(1)).update(libraryArgumentCaptor.capture());
        List<SearchIndexPaper> papersUpdatedInLibrary = libraryArgumentCaptor.getValue();
        assertThat(papersUpdatedInLibrary, hasSize(3));

        SearchIndexPaper updatedVersionOfFirstPaper = findSearchIndexPaperWithId(initialVersionOfFirstPaper.getId(), papersUpdatedInLibrary);
        assertThat(updatedVersionOfFirstPaper.getTitle(), is(equalTo(initialVersionOfFirstPaper.getTitle())));
        assertThat(updatedVersionOfFirstPaper.getAuthors()[0].getId(), is(equalTo(initialVersionOfFirstPaper.getAuthors()[0].getId())));
        assertThat(updatedVersionOfFirstPaper.getAuthors()[0].getName(), is(equalTo("Updated Author 1 Name")));

        SearchIndexPaper updatedVersionOfSecondPaper = findSearchIndexPaperWithId(initialVersionOfSecondPaper.getId(), papersUpdatedInLibrary);
        assertThat(updatedVersionOfSecondPaper.getTitle(), is(equalTo(initialVersionOfSecondPaper.getTitle())));
        assertThat(updatedVersionOfSecondPaper.getAuthors()[0].getId(), is(equalTo(initialVersionOfSecondPaper.getAuthors()[0].getId())));
        assertThat(updatedVersionOfSecondPaper.getAuthors()[0].getName(), is(equalTo("Original Author 2 Name")));
        assertThat(updatedVersionOfSecondPaper.getAuthors()[1].getId(), is(equalTo(initialVersionOfSecondPaper.getAuthors()[1].getId())));
        assertThat(updatedVersionOfSecondPaper.getAuthors()[1].getName(), is(equalTo("Updated Author 3 Name")));

        SearchIndexPaper updatedVersionOfThirdPaper = findSearchIndexPaperWithId(initialVersionOfThirdPaper.getId(), papersUpdatedInLibrary);
        assertThat(updatedVersionOfThirdPaper.getTitle(), is(equalTo(initialVersionOfThirdPaper.getTitle())));
        assertThat(updatedVersionOfThirdPaper.getAuthors()[0].getId(), is(equalTo(initialVersionOfThirdPaper.getAuthors()[0].getId())));
        assertThat(updatedVersionOfThirdPaper.getAuthors()[0].getName(), is(equalTo("Updated Author 3 Name")));
        assertThat(updatedVersionOfThirdPaper.getAuthors()[1].getId(), is(equalTo(initialVersionOfThirdPaper.getAuthors()[1].getId())));
        assertThat(updatedVersionOfThirdPaper.getAuthors()[1].getName(), is(equalTo("Original Author 4 Name")));
    }

    @Test
    public void shouldOnlyUpdateAuthorsRegistryAndLibraryWithLatestUpdateForEachAuthor() {
        // Given
        SearchIndexPaper initialVersionOfFirstPaper = aSearchIndexPaper()
                .withId("101")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("1").withNoName()
                )
                .build();

        Library library = mock(Library.class);
        AuthorUpdate.Author firstAuthorInitial = anAuthor().withId("1").withName("Initial Author 1 Name").build();
        AuthorUpdate.Author firstAuthorUpdated = anAuthor().withId("1").withName("Updated Author 1 Name").build();
        when(library.getPapersWrittenBy(collectionContainingOnly(firstAuthorUpdated)))
                .thenReturn(Stream.of(initialVersionOfFirstPaper));

        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        AuthorUpdatesStream authorUpdatesStream = mock(AuthorUpdatesStream.class);
        AuthorUpdatesSubscriber authorUpdatesSubscriber = new AuthorUpdatesSubscriber(library, authorRegistry);
        authorUpdatesSubscriber.subscribeTo(authorUpdatesStream);

        Consumer<List<AuthorUpdate>> consumerRegisteredOnAuthorUpdatesStream = getAuthorUpdatesConsumerRegisteredOn(authorUpdatesStream);


        List<AuthorUpdate> authorUpdates = asList(
                anAuthorUpdate().withAuthor(firstAuthorInitial).build(),
                anAuthorUpdate().withAuthor(firstAuthorUpdated).build()
        );

        // When
        consumerRegisteredOnAuthorUpdatesStream.accept(authorUpdates);

        // Then
        ArgumentCaptor<List<AuthorUpdate.Author>> authorRegistryArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRegistry, times(1)).update(authorRegistryArgumentCaptor.capture());
        List<AuthorUpdate.Author> authorsUpdatedInRegistry = authorRegistryArgumentCaptor.getValue();
        assertThat(authorsUpdatedInRegistry, hasSize(1));
        assertThat(authorsUpdatedInRegistry, contains(firstAuthorUpdated));

        ArgumentCaptor<List<SearchIndexPaper>> libraryArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library, times(1)).update(libraryArgumentCaptor.capture());
        List<SearchIndexPaper> papersUpdatedInLibrary = libraryArgumentCaptor.getValue();
        assertThat(papersUpdatedInLibrary, hasSize(1));

        SearchIndexPaper updatedVersionOfFirstPaper = papersUpdatedInLibrary.get(0);
        assertThat(updatedVersionOfFirstPaper.getAuthors()[0].getId(), is(equalTo("1")));
        assertThat(updatedVersionOfFirstPaper.getAuthors()[0].getName(), is(equalTo("Updated Author 1 Name")));
    }

    private static Consumer<List<AuthorUpdate>> getAuthorUpdatesConsumerRegisteredOn(AuthorUpdatesStream authorUpdatesStream) {
        ArgumentCaptor<Consumer<List<AuthorUpdate>>> argumentCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(authorUpdatesStream, times(1)).onAuthorUpdatesReceived(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private static SearchIndexPaper findSearchIndexPaperWithId(String id, List<SearchIndexPaper> searchIndexPapers) {
        return searchIndexPapers.stream().filter(searchIndexPaper -> searchIndexPaper.getId().equals(id)).findFirst().get();
    }

}