package com.ssrn.authors.domain;

import java.util.List;

public interface AuthorRepository extends AutoCloseable {
    void save(Author author);

    void save(List<Author> authors);

    Author getById(String id);

    boolean hasAuthor(String id);
}
