package com.ssrn.papers.replicator;

import com.google.common.collect.Iterators;
import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.PaperRepository;

import java.util.stream.Stream;

class PaperReplicator {
    private final OldPlatformPapersStreamSource oldPlatformPapersStreamSource;
    private final PaperRepository paperRepository;
    private final FeedJobCheckpointer<Paper> feedJobCheckpointer;

    PaperReplicator(OldPlatformPapersStreamSource oldPlatformPapersStreamSource, PaperRepository paperRepository, FeedJobCheckpointer<Paper> feedJobCheckpointer) {
        this.oldPlatformPapersStreamSource = oldPlatformPapersStreamSource;
        this.paperRepository = paperRepository;
        this.feedJobCheckpointer = feedJobCheckpointer;
    }

    void replicatePapers(Integer jobBatchSize, int databaseUpsertBatchSize) {
        Stream<Paper> paperStream = feedJobCheckpointer.getLastCheckpoint().map(oldPlatformPapersStreamSource::getPapersStreamAfterId)
                .orElseGet(oldPlatformPapersStreamSource::getPapersStream)
                .limit(jobBatchSize);

        Iterators.partition(paperStream.iterator(), databaseUpsertBatchSize).forEachRemaining(papers -> {
            paperRepository.save(papers);
            feedJobCheckpointer.checkpoint(papers.get(papers.size() - 1));
        });
    }
}
