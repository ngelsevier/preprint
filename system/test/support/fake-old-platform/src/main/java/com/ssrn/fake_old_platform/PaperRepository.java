package com.ssrn.fake_old_platform;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

class PaperRepository {
    private final List<Paper> papers = new CopyOnWriteArrayList<>();
    private final EventLog paperEventLog;

    PaperRepository(EventLog paperEventLog) {
        this.paperEventLog = paperEventLog;
    }

    Stream<Paper> getAll() {
        return papers.stream();
    }

    Paper getById(int abstractId) {
        return papers.stream().filter(paper -> paper.getId() == abstractId).findFirst().orElse(null);
    }

    public Paper[] getPapersWrittenBy(Participant participant) {
        return papers.stream()
                .filter(paper -> Arrays.stream(paper.getAuthorIds())
                        .anyMatch(authorId -> participant.getAccountId() == authorId))
                .toArray(Paper[]::new);
    }

    void save(Paper paper) {
        paper.appendUnsavedEventsTo(paperEventLog);

        synchronized (papers) {
            int paperId = paper.getId();
            papers.removeIf(p -> p.getId() == paperId);
            papers.add(paper);
        }
    }

    public void remove(int paperId) {
        synchronized (papers) {
            papers.removeIf(p -> p.getId() == paperId);
        }
    }
}
