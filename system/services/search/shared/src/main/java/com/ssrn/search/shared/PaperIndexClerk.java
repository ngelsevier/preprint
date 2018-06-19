package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.domain.Paper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaperIndexClerk {
    public List<SearchIndexPaper> prepareIndexablePapers(List<Paper> papers, AuthorUpdate.Author[] authors) {
        Map<String, AuthorUpdate.Author> authorsById = Arrays.stream(authors)
                .collect(Collectors.toMap(AuthorUpdate.Author::getId, Function.identity()));

        return papers.stream()
                .map(paper -> createSearchIndexPaper(paper, authorsById))
                .collect(Collectors.toList());
    }

    public List<SearchIndexPaper> prepareIndexablePapersRequiringUpdate(Stream<SearchIndexPaper> papersWrittenByAuthors, List<AuthorUpdate.Author> authors) {
        Map<String, AuthorUpdate.Author> updatedAuthorsById = authors.stream()
                .collect(Collectors.toMap(AuthorUpdate.Author::getId, Function.identity()));

        return papersWrittenByAuthors
                .map((SearchIndexPaper searchIndexPaper) -> searchIndexPaper.cloneIfUpdatedBy(updatedAuthorsById))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static SearchIndexPaper createSearchIndexPaper(Paper paper, Map<String, AuthorUpdate.Author> authorsById) {
        return new SearchIndexPaper(
                paper.getId(),
                paper.getTitle(),
                paper.getKeywords(),
                Arrays.stream(paper.getAuthorIds())
                        .map(authorId -> createSearchIndexPaperAuthor(String.valueOf(authorId), authorsById))
                        .toArray(SearchIndexPaperAuthor[]::new)
        );
    }

    private static SearchIndexPaperAuthor createSearchIndexPaperAuthor(String authorId, Map<String, AuthorUpdate.Author> authorsById) {
        return new SearchIndexPaperAuthor(
                authorId,
                Optional.ofNullable(authorsById.get(authorId))
                        .map(AuthorUpdate.Author::getName)
                        .orElse(null)
        );
    }
}
