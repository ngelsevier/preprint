package com.ssrn.authors.replicator;

import com.google.common.collect.Iterators;
import com.ssrn.authors.domain.Author;
import com.ssrn.authors.domain.AuthorRepository;

import java.util.stream.Stream;

class AuthorReplicator {
    private final OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource;
    private final AuthorRepository authorRepository;
    private final FeedJobCheckpointer<Author> feedJobCheckpointer;

    AuthorReplicator(OldPlatformAuthorsStreamSource oldPlatformAuthorsStreamSource, AuthorRepository authorRepository, FeedJobCheckpointer<Author> feedJobCheckpointer) {
        this.oldPlatformAuthorsStreamSource = oldPlatformAuthorsStreamSource;
        this.authorRepository = authorRepository;
        this.feedJobCheckpointer = feedJobCheckpointer;
    }

    void replicateAuthors(Integer jobBatchSize, int databaseUpsertBatchSize) {
        Stream<Author> authorStream = feedJobCheckpointer.getLastCheckpoint().map(oldPlatformAuthorsStreamSource::getAuthorsStreamAfterId)
                .orElseGet(oldPlatformAuthorsStreamSource::getAuthorsStream)
                .limit(jobBatchSize);

        Iterators.partition(authorStream.iterator(), databaseUpsertBatchSize).forEachRemaining(authors -> {
            authorRepository.save(authors);
            feedJobCheckpointer.checkpoint(authors.get(authors.size() - 1));
        });
    }
}