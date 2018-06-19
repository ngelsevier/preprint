package com.ssrn.search.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssrn.search.domain.AuthorUpdate;

import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchIndexPaper implements ElasticSearchDocument {

    private final String id;
    private final String title;
    private SearchIndexPaperAuthor[] authors;
    private String keywords;

    @JsonCreator
    public SearchIndexPaper(@JsonProperty(value = "id", required = true) String id,
                            @JsonProperty(value = "title", required = true) String title,
                            @JsonProperty(value = "keywords") String keywords,
                            @JsonProperty(value = "authors") SearchIndexPaperAuthor[] authors) {
        this.id = id;
        this.title = title;
        this.keywords = keywords;
        this.authors = authors;
    }

    Optional<SearchIndexPaper> cloneIfUpdatedBy(Map<String, AuthorUpdate.Author> updatedAuthors) {
        List<SearchIndexPaperAuthor> authorsToUpdate = getAuthorsToUpdate(updatedAuthors);

        return authorsToUpdate.size() == 0 ?
                Optional.empty() :
                Optional.of(new SearchIndexPaper(id, title, keywords, replaceAuthorsWith(authorsToUpdate)));
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getKeywords() {
        return keywords;
    }

    public SearchIndexPaperAuthor[] getAuthors() {
        return authors;
    }

    private List<SearchIndexPaperAuthor> getAuthorsToUpdate(Map<String, AuthorUpdate.Author> updatedAuthors) {
        return Arrays.stream(authors)
                .map(author -> {
                    AuthorUpdate.Author updatedAuthor = updatedAuthors.get(author.getId());
                    return updatedAuthor == null ? Optional.<SearchIndexPaperAuthor>empty() : author.cloneIfUpdatedBy(updatedAuthor);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private SearchIndexPaperAuthor[] replaceAuthorsWith(List<SearchIndexPaperAuthor> updatedAuthors) {
        return Arrays.stream(authors)
                .map(author -> updatedAuthors.stream()
                        .filter(updatedAuthor -> updatedAuthor.getId().equals(author.getId()))
                        .findFirst()
                        .orElse(author)
                )
                .toArray(SearchIndexPaperAuthor[]::new);
    }

    @Override
    public String toString() {
        return "SearchIndexPaper{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + Arrays.toString(authors) +
                ", keywords='" + keywords + '\'' +
                '}';
    }
}
