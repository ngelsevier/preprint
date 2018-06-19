package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;

import java.util.Collection;
import java.util.List;

public interface AuthorRegistry {
    AuthorUpdate.Author[] getByIds(Collection<String> authorIds);

    void update(List<AuthorUpdate.Author> authors);

    void delete(List<String> authorIds);
}
