package com.ssrn.fake_old_platform;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

class PaperEntitiesFeedService {
    private final PaperRepository paperRepository;

    PaperEntitiesFeedService(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    Stream<Paper> getStreamOfPapers(Optional<Integer> paperId) {
        return paperId
                .map(id -> paperRepository.getAll().filter(paper -> paper.getId() > id)).orElseGet(paperRepository::getAll)
                .sorted(Comparator.comparingInt(Paper::getId));
    }
}
