package com.ssrn.authors.replicator;

import com.ssrn.authors.domain.Author;

import java.util.stream.Stream;

public interface OldPlatformAuthorsStreamSource {
    Stream<Author> getAuthorsStream();

    Stream<Author> getAuthorsStreamAfterId(String id);
}
