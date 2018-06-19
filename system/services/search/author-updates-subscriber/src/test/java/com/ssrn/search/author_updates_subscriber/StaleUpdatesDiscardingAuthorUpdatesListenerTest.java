package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.domain.AuthorUpdate;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.function.Consumer;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.AuthorUpdateBuilder.anAuthorUpdate;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.*;

public class StaleUpdatesDiscardingAuthorUpdatesListenerTest {

    @Test
    public void shouldDelegateToProvidedConsumer() {
        // Given
        Consumer<List<AuthorUpdate>> delegateConsumer = mock(Consumer.class);

        StaleUpdatesDiscardingAuthorUpdatesListener staleUpdatesDiscardingAuthorUpdatesListener = new StaleUpdatesDiscardingAuthorUpdatesListener(
                delegateConsumer
        );

        AuthorUpdate firstUpdate = anAuthorUpdate().build();
        AuthorUpdate secondUpdate = anAuthorUpdate().build();
        List<AuthorUpdate> authorUpdates = asList(firstUpdate, secondUpdate);

        // When
        staleUpdatesDiscardingAuthorUpdatesListener.notify(authorUpdates);

        // Then
        ArgumentCaptor<List<AuthorUpdate>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(delegateConsumer, times(1)).accept(listArgumentCaptor.capture());
        List<AuthorUpdate> authorUpdatesPassedToDelegate = listArgumentCaptor.getValue();

        assertThat(authorUpdatesPassedToDelegate, hasSize(2));
        assertThat(authorUpdatesPassedToDelegate.get(0), is(sameInstance(firstUpdate)));
        assertThat(authorUpdatesPassedToDelegate.get(1), is(sameInstance(secondUpdate)));
    }

    @Test
    public void shouldOnlyNotifyDelegateListenerAboutMostRecentUpdateForEachAuthorInABatch() {
        // Given
        Consumer<List<AuthorUpdate>> delegateConsumer = mock(Consumer.class);

        StaleUpdatesDiscardingAuthorUpdatesListener staleUpdatesDiscardingAuthorUpdatesListener = new StaleUpdatesDiscardingAuthorUpdatesListener(
                delegateConsumer
        );

        AuthorUpdate firstUpdateToAuthor1 = anAuthorUpdate().withAuthor(anAuthor().withId("1").withName("Author 1 Original Name")).build();
        AuthorUpdate firstUpdateToAuthor2 = anAuthorUpdate().withAuthor(anAuthor().withId("2").withName("Author 2 Original Name")).build();
        AuthorUpdate onlyUpdateToAuthor3 = anAuthorUpdate().withAuthor(anAuthor().withId("3").withName("Author 3 Original Name")).build();
        AuthorUpdate secondUpdateToAuthor1 = anAuthorUpdate().withAuthor(anAuthor().withId("1").withName("Author 1 Updated Name")).build();
        AuthorUpdate secondUpdateToAuthor2 = anAuthorUpdate().withAuthor(anAuthor().withId("2").withName("Author 2 Updated Name")).build();

        List<AuthorUpdate> authorUpdates = asList(
                firstUpdateToAuthor1,
                firstUpdateToAuthor2,
                onlyUpdateToAuthor3,
                secondUpdateToAuthor1,
                secondUpdateToAuthor2
        );

        // When
        staleUpdatesDiscardingAuthorUpdatesListener.notify(authorUpdates);

        // Then
        ArgumentCaptor<List<AuthorUpdate>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(delegateConsumer, times(1)).accept(listArgumentCaptor.capture());
        List<AuthorUpdate> authorUpdatesPassedToDelegate = listArgumentCaptor.getValue();

        assertThat(authorUpdatesPassedToDelegate, hasSize(3));
        assertThat(authorUpdatesPassedToDelegate, containsInAnyOrder(secondUpdateToAuthor1, secondUpdateToAuthor2, onlyUpdateToAuthor3));
    }

    @Test
    public void shouldNotifyDelegateListenerAboutUpdatesInOrderTheyOccurInBatch() {
        // Given
        Consumer<List<AuthorUpdate>> delegateConsumer = mock(Consumer.class);

        StaleUpdatesDiscardingAuthorUpdatesListener staleUpdatesDiscardingAuthorUpdatesListener = new StaleUpdatesDiscardingAuthorUpdatesListener(
                delegateConsumer
        );

        AuthorUpdate firstUpdateToAuthor1 = anAuthorUpdate().withAuthor(anAuthor().withId("1").withName("Author 1 Original Name")).build();
        AuthorUpdate firstUpdateToAuthor2 = anAuthorUpdate().withAuthor(anAuthor().withId("2").withName("Author 2 Original Name")).build();
        AuthorUpdate onlyUpdateToAuthor3 = anAuthorUpdate().withAuthor(anAuthor().withId("3").withName("Author 3 Original Name")).build();
        AuthorUpdate secondUpdateToAuthor1 = anAuthorUpdate().withAuthor(anAuthor().withId("1").withName("Author 1 Updated Name")).build();
        AuthorUpdate secondUpdateToAuthor2 = anAuthorUpdate().withAuthor(anAuthor().withId("2").withName("Author 2 Updated Name")).build();
        AuthorUpdate thirdUpdateToAuthor1 = anAuthorUpdate().withAuthor(anAuthor().withId("1").withName("Author 1 Updated Again Name")).build();

        List<AuthorUpdate> authorUpdates = asList(
                firstUpdateToAuthor1,
                firstUpdateToAuthor2,
                secondUpdateToAuthor1,
                secondUpdateToAuthor2,
                onlyUpdateToAuthor3,
                thirdUpdateToAuthor1
        );

        // When
        staleUpdatesDiscardingAuthorUpdatesListener.notify(authorUpdates);

        // Then
        ArgumentCaptor<List<AuthorUpdate>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(delegateConsumer, times(1)).accept(listArgumentCaptor.capture());
        List<AuthorUpdate> authorUpdatesPassedToDelegate = listArgumentCaptor.getValue();

        assertThat(authorUpdatesPassedToDelegate, hasSize(3));
        assertThat(authorUpdatesPassedToDelegate.get(0), is(sameInstance(secondUpdateToAuthor2)));
        assertThat(authorUpdatesPassedToDelegate.get(1), is(sameInstance(onlyUpdateToAuthor3)));
        assertThat(authorUpdatesPassedToDelegate.get(2), is(sameInstance(thirdUpdateToAuthor1)));
    }
}