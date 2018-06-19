package com.ssrn.fake_old_platform;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class AuthorEntitiesFeedService {
    private final ParticipantRepository participantRepository;

    AuthorEntitiesFeedService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    Stream<Author> getStreamOfAuthors(Optional<Integer> authorId) {
        return authorId.map(this::getAllAuthorsAfter).orElseGet(this::getAllAuthors)
                .sorted(Comparator.comparingInt(Author::getId));
    }

    private Stream<Author> getAllAuthorsAfter(Integer authorId) {
        return getAllAuthors().filter(author -> author.getId() > authorId);
    }

    private Stream<Author> getAllAuthors() {
        return participantRepository.getAll()
                .filter(participant -> participant.isAnAuthor(Optional.empty()))
                .map(Author::new);
    }
}
