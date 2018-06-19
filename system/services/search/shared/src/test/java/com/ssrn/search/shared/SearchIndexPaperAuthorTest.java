package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import org.junit.Test;

import java.util.Optional;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchIndexPaperAuthorTest {
    @Test
    public void shouldCloneSelfWithUpdatedName() {
        // Given
        SearchIndexPaperAuthor searchIndexPaperAuthor = aSearchIndexPaperAuthor().withId("12").withName("Initial Name").build();

        AuthorUpdate.Author updatedAuthor = anAuthor().withId("12").withName("Updated Name").build();

        // When
        Optional<SearchIndexPaperAuthor> updatedSearchIndexPaperAuthor = searchIndexPaperAuthor.cloneIfUpdatedBy(updatedAuthor);

        // Then
        assertThat(updatedSearchIndexPaperAuthor.isPresent(), is(equalTo(true)));
        assertThat(updatedSearchIndexPaperAuthor.get().getId(), is(equalTo("12")));
        assertThat(updatedSearchIndexPaperAuthor.get().getName(), is(equalTo("Updated Name")));
    }

    @Test
    public void shouldNotCloneSelfIfNameUpToDate() {
        // Given
        SearchIndexPaperAuthor searchIndexPaperAuthor = aSearchIndexPaperAuthor().withId("12").withName("Updated Name").build();

        AuthorUpdate.Author updatedAuthor = anAuthor().withId("12").withName("Updated Name").build();

        // When
        Optional<SearchIndexPaperAuthor> updatedSearchIndexPaperAuthor = searchIndexPaperAuthor.cloneIfUpdatedBy(updatedAuthor);

        // Then
        assertThat(updatedSearchIndexPaperAuthor.isPresent(), is(equalTo(false)));
    }

    @Test
    public void shouldSupportUpdatingAuthorWithNoName() {
        // Given
        SearchIndexPaperAuthor searchIndexPaperAuthor = aSearchIndexPaperAuthor().withId("12").withNoName().build();

        AuthorUpdate.Author updatedAuthor = anAuthor().withId("12").withName("Updated Name").build();

        // When
        Optional<SearchIndexPaperAuthor> updatedSearchIndexPaperAuthor = searchIndexPaperAuthor.cloneIfUpdatedBy(updatedAuthor);

        // Then
        assertThat(updatedSearchIndexPaperAuthor.isPresent(), is(equalTo(true)));
        assertThat(updatedSearchIndexPaperAuthor.get().getId(), is(equalTo("12")));
        assertThat(updatedSearchIndexPaperAuthor.get().getName(), is(equalTo("Updated Name")));
    }

}