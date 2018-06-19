package com.ssrn.search.shared;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PaperSearchResultTest {

    @Test
    public void shouldReturnHighlightedTitleIfPresent() {
        // Given
       Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("title", new String[]{"<em>my</em> title"});
        }};

        PaperSearchResult paperSearchResult = new PaperSearchResult("id", "my title", "keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(paperSearchResult.getTitle(), is("<em>my</em> title"));
    }

    @Test
    public void shouldReturnMultipleHighlightedFieldsInTitle() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("title", new String[]{"<em>my</em> crazy <em>big</em> title"});
        }};

        PaperSearchResult paperSearchResult = new PaperSearchResult("id", "my crazy big title", "keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(paperSearchResult.getTitle(), is("<em>my</em> crazy <em>big</em> title"));
    }

    @Test
    public void shouldReturnNonHighlightedTitleWhenThereAreNoHighlightedFieldsForTitle() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("keywords", new String[]{"<em>my</em> keywords"});
        }};

        PaperSearchResult psr = new PaperSearchResult("id", "paper title", "my keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(psr.getTitle(), is("paper title"));
    }

    @Test
    public void shouldReturnNonHighlightedTitleWhenThereAreNoHighlightedFields() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<>();

        PaperSearchResult psr = new PaperSearchResult("id", "paper title", "my keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(psr.getTitle(), is("paper title"));
    }

    @Test
    public void shouldReturnHighlightedKeywordsIfPresent() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("keywords", new String[]{"<em>my</em> keywords"});
        }};

        PaperSearchResult psr = new PaperSearchResult("id", "paper title", "my keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(psr.getKeywords(), is("<em>my</em> keywords"));
    }

    @Test
    public void shouldReturnNonHighlightedKeywordsWhenThereAreNoHighlightedFieldsForKeywords() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<String, String[]>() {{
            put("title", new String[]{"<em>my</em> title"});
        }};

        PaperSearchResult psr = new PaperSearchResult("id", "my title", "keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(psr.getKeywords(), is("keywords"));
    }

    @Test
    public void shouldReturnNonHighlightedKeywordsWhenThereAreNoHighlightedFields() {
        // Given
        Map<String, String[]> highlightedFields = new HashMap<>();
        PaperSearchResult psr = new PaperSearchResult("id", "my title", "keywords", new SearchResultAuthor[0], highlightedFields);

        // When, Then
        assertThat(psr.getKeywords(), is("keywords"));
    }
}