package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface Library {
    SearchResults searchForItemsMatching(String searchTerm, int from, int size);

    void update(List<SearchIndexPaper> papers);

    Stream<SearchIndexPaper> getPapersWrittenBy(Collection<AuthorUpdate.Author> authors);

    void delete(List<String> paperIds);
}