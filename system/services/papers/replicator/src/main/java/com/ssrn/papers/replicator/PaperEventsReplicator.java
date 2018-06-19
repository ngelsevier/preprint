package com.ssrn.papers.replicator;

import com.ssrn.papers.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ssrn.papers.domain.SubmissionStage.*;

class PaperEventsReplicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperEventsReplicator.class);
    private final OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource;
    private final PaperRepository paperRepository;
    private final FeedJobCheckpointer<Paper.Event> feedJobCheckpointer;

    PaperEventsReplicator(OldPlatformPaperEventsStreamSource oldPlatformPaperEventsStreamSource, PaperRepository paperRepository, FeedJobCheckpointer<Paper.Event> feedJobCheckpointer) {
        this.oldPlatformPaperEventsStreamSource = oldPlatformPaperEventsStreamSource;
        this.paperRepository = paperRepository;
        this.feedJobCheckpointer = feedJobCheckpointer;
    }

    void replicateNewEvents() {
        feedJobCheckpointer.getLastCheckpoint()
                .map(oldPlatformPaperEventsStreamSource::getEventsStreamStartingAfter)
                .orElseGet(oldPlatformPaperEventsStreamSource::getEventsStream)
                .forEach(event -> {
                    if (event instanceof Paper.DraftedEvent) {
                        Paper.DraftedEvent draftedEvent = (Paper.DraftedEvent) event;
                        if (!paperRepository.hasPaper(draftedEvent.getEntityId())) {
                            createPaperFrom(draftedEvent);
                        }
                    } else {
                        updatePaperWith(event);
                    }

                    feedJobCheckpointer.checkpoint(event);
                });
    }

    private void createPaperFrom(Paper.DraftedEvent event) {
        paperRepository.save(new Paper(
                event.getEntityId(),
                event.getEntityVersion(),
                event.getTitle(),
                null,
                event.getAuthorIds(),
                event.isPaperPrivate(),
                event.isPaperIrrelevant(),
                event.isPaperRestricted(),
                IN_DRAFT));
    }

    private void updatePaperWith(Paper.Event event) {
        Paper paper = findPaperForEvent(event);

        if (paper != null && applyEventTo(paper, event)) {
            paperRepository.save(paper);
        }
    }

    private boolean applyEventTo(Paper paper, Paper.Event event) {
        try {
            event.applyOn(paper);
            return true;
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            LOGGER.warn(String.format("Exception thrown whilst attempting to apply event: %s", e.getEvent()), e);
            return false;
        }
    }

    private Paper findPaperForEvent(Paper.Event event) {
        try {
            return paperRepository.getById(event.getEntityId());
        } catch (PaperNotFoundException e) {
            LOGGER.warn(String.format("Paper with ID '%s' not found when attempting to process event: %s", event.getEntityId(), event), e);
            return null;
        }
    }

}
