package com.ssrn.frontend.website.fake_search_service;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaperSearchResult.class, name = "Paper"),
        @JsonSubTypes.Type(value = AuthorSearchResult.class, name = "Author")
})
public abstract class BaseSearchResult {
    private final String id;
    private String searchQuery;

    public BaseSearchResult(String id, String searchQuery) {
        this.id = id;
        this.searchQuery = searchQuery;
    }

    public String getId() {
        return id;
    }

    boolean searchQueryHasAMatchIn(String name) {
        return name != null && name.toLowerCase().contains(searchQuery.toLowerCase());
    }

    String applyHighlights(String name) {
        List<String> endResultWrapper = new ArrayList<>();
        endResultWrapper.add(name);
        Arrays.stream(searchQuery.split(" ")).forEach(
                searchTerm -> {
                    String stringToBeHighlighted = endResultWrapper.get(0);
                    int indexOfSearchTerm = stringToBeHighlighted.toLowerCase().indexOf(searchTerm.toLowerCase());
                    if (indexOfSearchTerm >= 0) {
                        String originalWord = stringToBeHighlighted.substring(indexOfSearchTerm, indexOfSearchTerm + searchTerm.length());
                        String highlightedString = stringToBeHighlighted.replaceAll(originalWord, String.format("<em>%s</em>", originalWord));
                        endResultWrapper.set(0, highlightedString);
                    }
                }
        );
        return endResultWrapper.get(0);
    }

}

class PaperSearchResult extends BaseSearchResult {
    private final String title;

    private final String keywords;

    private final IndexedPaperAuthor[] authors;

    public PaperSearchResult(String id, String title, String keywords, IndexedPaperAuthor[] authors, String query) {
        super(id, query);
        this.title = title;
        this.keywords = keywords;
        this.authors = authors;
    }

    public String getTitle() {
        return searchQueryHasAMatchIn(title) ? applyHighlights(title) : title;
    }

    public String getKeywords() {
        return searchQueryHasAMatchIn(keywords) ?  applyHighlights(keywords) : keywords;
    }

    public IndexedPaperAuthor[] getAuthors() {
        return Arrays.stream(authors)
                .map(author ->
                        searchQueryHasAMatchIn(author.getName()) ?
                                new IndexedPaperAuthor(author.getId(), applyHighlights(author.getName())) :
                                author
                ).toArray(IndexedPaperAuthor[]::new);
    }

    public Integer[] getAuthorIds() {
        return Arrays.stream(authors).map(IndexedPaperAuthor::getId).map(Integer::parseInt).toArray(Integer[]::new);
    }
}

class AuthorSearchResult extends BaseSearchResult {
    private final String name;

    public AuthorSearchResult(String id, String name, String query) {
        super(id, query);
        this.name = name;
    }

    public String getName() {
        return searchQueryHasAMatchIn(name) ? applyHighlights(name) : name;
    }
}