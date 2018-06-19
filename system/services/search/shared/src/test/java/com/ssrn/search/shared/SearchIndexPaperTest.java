package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static com.ssrn.search.shared.SearchIndexPaperBuilder.aSearchIndexPaper;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;

public class SearchIndexPaperTest {
    @Test
    public void shouldCloneSelfWithUpdatedAuthorsInOriginalOrder() {
        SearchIndexPaper searchIndexPaper = aSearchIndexPaper()
                .withId("12")
                .withTitle("Initial Title")
                .withKeywords("random string")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("4").withName("Updated Author 4 Name"),
                        aSearchIndexPaperAuthor().withId("5").withName("Initial Author 5 Name"),
                        aSearchIndexPaperAuthor().withId("6").withName("Initial Author 6 Name")
                )
                .build();

        HashMap<String, AuthorUpdate.Author> authorUpdates = new HashMap<String, AuthorUpdate.Author>() {{
            put("4", anAuthor().withId("4").withName("Updated Author 4 Name").build());
            put("5", anAuthor().withId("5").withName("Updated Author 5 Name").build());
            put("6", anAuthor().withId("6").withName("Updated Author 6 Name").build());
        }};

        Optional<SearchIndexPaper> cloneResult = searchIndexPaper.cloneIfUpdatedBy(authorUpdates);
        assertThat(cloneResult.isPresent(), is(equalTo(true)));

        SearchIndexPaper updatedSearchIndexPaper = cloneResult.get();
        assertThat(updatedSearchIndexPaper.getTitle(), is(equalTo("Initial Title")));
        assertThat(updatedSearchIndexPaper.getKeywords(), is(equalTo("random string")));

        assertThat(updatedSearchIndexPaper.getAuthors(), is(arrayWithSize(3)));
        assertThat(updatedSearchIndexPaper.getAuthors()[0].getId(), is(equalTo("4")));
        assertThat(updatedSearchIndexPaper.getAuthors()[0].getName(), is(equalTo("Updated Author 4 Name")));
        assertThat(updatedSearchIndexPaper.getAuthors()[1].getId(), is(equalTo("5")));
        assertThat(updatedSearchIndexPaper.getAuthors()[1].getName(), is(equalTo("Updated Author 5 Name")));
        assertThat(updatedSearchIndexPaper.getAuthors()[2].getId(), is(equalTo("6")));
        assertThat(updatedSearchIndexPaper.getAuthors()[2].getName(), is(equalTo("Updated Author 6 Name")));
    }

    @Test
    public void shouldSupportUpdatingAuthorsWhoseNamesAreNotSet() {
        SearchIndexPaper searchIndexPaper = aSearchIndexPaper()
                .withId("12")
                .withTitle("Initial Title")
                .withAuthors(aSearchIndexPaperAuthor().withId("4").withNoName())
                .build();

        HashMap<String, AuthorUpdate.Author> authorUpdates = new HashMap<String, AuthorUpdate.Author>() {{
            put("4", anAuthor().withId("4").withName("Updated Author 4 Name").build());
        }};

        Optional<SearchIndexPaper> cloneResult = searchIndexPaper.cloneIfUpdatedBy(authorUpdates);
        assertThat(cloneResult.isPresent(), is(equalTo(true)));

        SearchIndexPaper updatedSearchIndexPaper = cloneResult.get();

        assertThat(updatedSearchIndexPaper.getAuthors(), is(arrayWithSize(1)));
        assertThat(updatedSearchIndexPaper.getAuthors()[0].getId(), is(equalTo("4")));
        assertThat(updatedSearchIndexPaper.getAuthors()[0].getName(), is(equalTo("Updated Author 4 Name")));
    }

    @Test
    public void shouldNotCloneSelfIfAllAuthorsAreUpToDate() {
        SearchIndexPaper searchIndexPaper = aSearchIndexPaper()
                .withId("12")
                .withTitle("Initial Title")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("5").withName("Updated Author 5 Name"),
                        aSearchIndexPaperAuthor().withId("6").withName("Updated Author 6 Name")
                )
                .build();

        HashMap<String, AuthorUpdate.Author> authorUpdates = new HashMap<String, AuthorUpdate.Author>() {{
            put("5", anAuthor().withId("5").withName("Updated Author 5 Name").build());
            put("6", anAuthor().withId("6").withName("Updated Author 6 Name").build());
        }};

        Optional<SearchIndexPaper> cloneResult = searchIndexPaper.cloneIfUpdatedBy(authorUpdates);
        assertThat(cloneResult.isPresent(), is(equalTo(false)));
    }

    @Test
    public void shouldRetainAnyExistingAuthorsAbsentFromProvidedAuthorUpdates() {
        SearchIndexPaper searchIndexPaper = aSearchIndexPaper()
                .withId("12")
                .withTitle("Initial Title")
                .withAuthors(
                        aSearchIndexPaperAuthor().withId("5").withName("Initial Author 5 Name"),
                        aSearchIndexPaperAuthor().withId("6").withName("Initial Author 6 Name")
                )
                .build();

        HashMap<String, AuthorUpdate.Author> authorUpdates = new HashMap<String, AuthorUpdate.Author>() {{
            put("6", anAuthor().withId("6").withName("Updated Author 6 Name").build());
        }};

        Optional<SearchIndexPaper> cloneResult = searchIndexPaper.cloneIfUpdatedBy(authorUpdates);
        assertThat(cloneResult.isPresent(), is(equalTo(true)));

        SearchIndexPaper updatedSearchIndexPaper = cloneResult.get();
        assertThat(updatedSearchIndexPaper.getTitle(), is(equalTo("Initial Title")));

        assertThat(updatedSearchIndexPaper.getAuthors(), is(arrayWithSize(2)));
        assertThat(updatedSearchIndexPaper.getAuthors()[0].getId(), is(equalTo("5")));
        assertThat(updatedSearchIndexPaper.getAuthors()[0].getName(), is(equalTo("Initial Author 5 Name")));
        assertThat(updatedSearchIndexPaper.getAuthors()[1].getId(), is(equalTo("6")));
        assertThat(updatedSearchIndexPaper.getAuthors()[1].getName(), is(equalTo("Updated Author 6 Name")));
    }
}
