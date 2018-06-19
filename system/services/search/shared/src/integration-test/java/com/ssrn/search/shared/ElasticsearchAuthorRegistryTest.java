package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class ElasticsearchAuthorRegistryTest extends ElasticsearchTest {

    @Test
    public void shouldReturnAnEmptyArrayWhenAuthorsNotFoundInAuthorsIndex() {
        // Given
        ElasticsearchAuthorRegistry registry = new ElasticsearchAuthorRegistry(
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort()
        );

        // When
        AuthorUpdate.Author[] authors = registry.getByIds(singletonList("101"));

        // Then
        assertThat(authors, arrayWithSize(0));
    }

    @Test
    public void shouldFindAuthorsInAuthorsIndex() {
        // Given
        ElasticsearchAuthorRegistry registry = new ElasticsearchAuthorRegistry(
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort()
        );

        registry.update(asList(
                anAuthor().withId("101").withName("John Doe").withRemoved(false).build(),
                anAuthor().withId("102").withName("Jane Doe").withRemoved(false).build()
        ));

        // When
        AuthorUpdate.Author[] authors = registry.getByIds(Arrays.asList("101", "102"));

        // Then
        assertThat(authors, arrayWithSize(2));

        assertThat(authors[0].getId(), is(equalTo("101")));
        assertThat(authors[0].getName(), is(equalTo("John Doe")));

        assertThat(authors[1].getId(), is(equalTo("102")));
        assertThat(authors[1].getName(), is(equalTo("Jane Doe")));

    }

    @Test
    public void shouldRemoveAuthorsInAuthorsIndex() {
        // Given
        ElasticsearchAuthorRegistry registry = new ElasticsearchAuthorRegistry(
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort()
        );

        registry.update(asList(
                anAuthor().withId("101").withName("John Doe").build(),
                anAuthor().withId("102").withName("Jane Doe").build(),
                anAuthor().withId("103").withName("Josh Doe").build()
        ));

        // When
        AuthorUpdate.Author[] authors = registry.getByIds(Arrays.asList("101", "102", "103"));

        // Then
        assertThat(authors, arrayWithSize(3));

        assertThat(authors[0].getId(), is(equalTo("101")));
        assertThat(authors[0].getName(), is(equalTo("John Doe")));

        assertThat(authors[1].getId(), is(equalTo("102")));
        assertThat(authors[1].getName(), is(equalTo("Jane Doe")));

        assertThat(authors[2].getId(), is(equalTo("103")));
        assertThat(authors[2].getName(), is(equalTo("Josh Doe")));

        registry.delete(asList("101","102"));

        // When
        AuthorUpdate.Author[] authorsAfterRemoval = registry.getByIds(Arrays.asList("101", "102", "103"));

        // Then
        assertThat(authorsAfterRemoval, arrayWithSize(1));

        assertThat(authorsAfterRemoval[0].getId(), is(equalTo("103")));
        assertThat(authorsAfterRemoval[0].getName(), is(equalTo("Josh Doe")));
    }

    @Test
    public void shouldUpdateNameOfAnExistingAuthorWhenAuthorUpdateIsReceived() {
        // Given
        ElasticsearchAuthorRegistry registry = new ElasticsearchAuthorRegistry(
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort()
        );

        String id = Integer.toString(new Random().nextInt(9999));
        registry.update(singletonList(anAuthor().withId(id).withNoName().build()));

        AuthorUpdate.Author[] authors = registry.getByIds(singletonList(id));
        assertThat(authors, arrayWithSize(1));
        assertThat(authors[0].getId(), is(equalTo(id)));
        assertThat(authors[0].getName(), is(nullValue()));

        // When
        registry.update(singletonList(anAuthor().withId(id).withName("Cassius Marcellus Clay Jr").build()));

        // Then
        AuthorUpdate.Author[] authorsOnFirstUpdate = registry.getByIds(singletonList(id));
        assertThat(authorsOnFirstUpdate, arrayWithSize(1));
        assertThat(authorsOnFirstUpdate[0].getId(), is(equalTo(id)));
        assertThat(authorsOnFirstUpdate[0].getName(), is(equalTo("Cassius Marcellus Clay Jr")));

        // When
        registry.update(singletonList(anAuthor().withId(id).withName("Muhammad Ali").build()));

        // Then
        AuthorUpdate.Author[] authorsSecondUpdate = registry.getByIds(singletonList(id));
        assertThat(authorsSecondUpdate, arrayWithSize(1));
        assertThat(authorsSecondUpdate[0].getId(), is(equalTo(id)));
        assertThat(authorsSecondUpdate[0].getName(), is(equalTo("Muhammad Ali")));

    }
}
