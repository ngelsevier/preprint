package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.domain.Paper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.AuthorUpdateBuilder.anAuthorUpdate;
import static com.ssrn.search.shared.PaperBuilder.aPaper;
import static com.ssrn.search.shared.matchers.mockito.CollectionContainingOnlyMatcher.collectionContainingOnly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class AuthorRegistrarTest {

    @Test
    public void shouldUpdateAndDeleteAllAuthorsInAuthorsRegistry() {
        // Given
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        AuthorRegistrar authorRegistrar = new AuthorRegistrar(authorRegistry);

        List<AuthorUpdate> authorUpdates = asList(
                anAuthorUpdate().withAuthor(anAuthor().withName("First Author").withId("1").withRemoved(false)).build(),
                anAuthorUpdate().withAuthor(anAuthor().withName("Second Author").withId("2").withRemoved(false)).build(),
                anAuthorUpdate().withAuthor(anAuthor().withName("Third Author").withId("3").withRemoved(true)).build(),
                anAuthorUpdate().withAuthor(anAuthor().withName("fourth Author").withId("4").withRemoved(true)).build()

        );

        // When
        authorRegistrar.updateRegistry(authorUpdates);

        // Then
        ArgumentCaptor<List<AuthorUpdate.Author>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRegistry, times(1)).update(listArgumentCaptor.capture());

        List<AuthorUpdate.Author> updatedAuthors = listArgumentCaptor.getValue();
        assertThat(updatedAuthors, hasSize(2));

        AuthorUpdate.Author firstUpdatedAuthor = updatedAuthors.get(0);
        assertThat(firstUpdatedAuthor.getId(), is(equalTo("1")));
        assertThat(firstUpdatedAuthor.getName(), is(equalTo("First Author")));

        AuthorUpdate.Author secondAuthor = updatedAuthors.get(1);
        assertThat(secondAuthor.getId(), is(equalTo("2")));
        assertThat(secondAuthor.getName(), is(equalTo("Second Author")));


        ArgumentCaptor<List<String>> stringArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(authorRegistry, times(1)).delete(stringArgumentCaptor.capture());

        List<String> deletedAuthorIds = stringArgumentCaptor.getValue();
        assertThat(deletedAuthorIds, hasSize(2));
        assertThat(deletedAuthorIds.get(0), is(equalTo("3")));
        assertThat(deletedAuthorIds.get(1), is(equalTo("4")));

    }

    @Test
    public void shouldRetrieveUniqueSetOfAuthorsFromRegistry() {
        // Given
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        AuthorRegistrar authorRegistrar = new AuthorRegistrar(authorRegistry);

        List<Paper> batchOfPapers = asList(
                aPaper().withAuthorIds("1", "2").build(),
                aPaper().withAuthorIds("2", "3").build()
        );

        AuthorUpdate.Author aRetrievedAuthor = anAuthor().build();
        AuthorUpdate.Author anotherRetrievedAuthor = anAuthor().build();

        when(authorRegistry.getByIds(collectionContainingOnly("1", "2", "3")))
                .thenReturn(new AuthorUpdate.Author[]{aRetrievedAuthor, anotherRetrievedAuthor});

        // When
        AuthorUpdate.Author[] authors = authorRegistrar.getAuthorsWhoHaveWritten(batchOfPapers);

        // Then
        assertThat(authors, arrayContainingInAnyOrder(
                is(sameInstance(aRetrievedAuthor)),
                is(sameInstance(anotherRetrievedAuthor))
        ));

        verify(authorRegistry, times(1)).getByIds(any());
    }
}