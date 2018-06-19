package com.ssrn.search.shared;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SearchResultAuthorTest {

    @Test
    public void shouldReturnHighlightedAuthorNameIfPresent() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("authors.name", new String[]{"<em>Billy</em> bob"});
        }};
        SearchResultAuthor author = new SearchResultAuthor("101", "Billy bob", highlightedFields);

        // When, Then
        assertThat(author.getName(), is(equalTo("<em>Billy</em> bob")));
    }

    @Test
    public void shouldReturnNonHighlightedNameWhenAuthorNameDoesNotMatchHighlightedFields() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("authors.name", new String[]{"<em>Tommy</em> John"});
        }};
        SearchResultAuthor author = new SearchResultAuthor("101", "Billy bob", highlightedFields);

        // When, Then
        assertThat(author.getName(), is(equalTo("Billy bob")));
    }

    @Test
    public void shouldReturnNonHighlightedNameWhenThereAreNoHighlightedFields() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<>();
        SearchResultAuthor author = new SearchResultAuthor("101", "Billy bob", highlightedFields);

        // When, Then
        assertThat(author.getName(), is(equalTo("Billy bob")));
    }
}