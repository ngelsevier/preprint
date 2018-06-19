package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.domain.Paper;

import java.util.*;
import java.util.stream.Stream;

import static com.ssrn.search.domain.AuthorUpdate.Author;
import static java.util.stream.Collectors.toList;

public class Librarian {
    private final Library library;
    private final AuthorRegistrar authorRegistrar;
    private final PaperIndexClerk paperIndexClerk;

    public Librarian(Library elasticsearchLibrary, AuthorRegistry authorRegistry) {
        library = elasticsearchLibrary;
        authorRegistrar = new AuthorRegistrar(authorRegistry);
        paperIndexClerk = new PaperIndexClerk();
    }

    public void updatePapers(List<Paper> papers) {
        List<String> nonSearchablePaperIds = papers.stream().filter(paper -> !paper.isPaperSearchable()).map(Paper::getId).distinct().collect(toList());
        List<Paper> searchablePapers = papers.stream().filter(Paper::isPaperSearchable).collect(toList());
        library.delete(nonSearchablePaperIds);

        if (searchablePapers.size() > 0) {
            Author[] authors = authorRegistrar.getAuthorsWhoHaveWritten(searchablePapers);
            List<SearchIndexPaper> indexablePapers = paperIndexClerk.prepareIndexablePapers(searchablePapers, authors);
            library.update(indexablePapers);
        }
    }

    public void applyAuthorUpdates(List<AuthorUpdate> authorUpdates) {
        List<Author> authors = authorUpdates.stream().map(AuthorUpdate::getAuthor).collect(toList());
        Stream<SearchIndexPaper> indexablePapers = library.getPapersWrittenBy(authors);
        List<SearchIndexPaper> indexablePapersToUpdate = paperIndexClerk.prepareIndexablePapersRequiringUpdate(indexablePapers, authors);

        library.update(indexablePapersToUpdate);
    }
}
