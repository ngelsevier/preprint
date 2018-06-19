package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.domain.Paper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class AuthorRegistrar {
    private final AuthorRegistry authorRegistry;

    public AuthorRegistrar(AuthorRegistry authorRegistry) {
        this.authorRegistry = authorRegistry;
    }

    public void updateRegistry(List<AuthorUpdate> authorUpdates) {
        Map<Boolean, List<AuthorUpdate>> authorsTobeUpdated = authorUpdates.stream()
                .collect(Collectors.partitioningBy(authorUpdate -> !authorUpdate.getAuthor().isRemoved()));

        List<AuthorUpdate> updatedAuthors = authorsTobeUpdated.get(true);
        authorRegistry.update(updatedAuthors.stream()
                .map(AuthorUpdate::getAuthor)
                .collect(Collectors.toList()));

        List<AuthorUpdate> authorsForRemoval = authorsTobeUpdated.get(false);
        authorRegistry.delete(authorsForRemoval.stream().map(author -> author.getId()).collect(toList()));
    }

    public AuthorUpdate.Author[] getAuthorsWhoHaveWritten(List<Paper> papers) {
        List<String> authorIds = papers.stream()
                .flatMap(paper -> Arrays.stream(paper.getAuthorIds()))
                .distinct()
                .collect(Collectors.toList());

        return authorRegistry.getByIds(authorIds);
    }

}
