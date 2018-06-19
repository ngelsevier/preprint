package com.ssrn.authors.replicator;

import com.ssrn.authors.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ssrn.authors.domain.EventData.deserializeToEventData;

class AuthorEventsProjector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorEventsProjector.class);
    private final OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource;
    private final AuthorRepository authorRepository;
    private final FeedJobCheckpointer<Event> feedJobCheckpointer;

    AuthorEventsProjector(OldPlatformAuthorEventsStreamSource oldPlatformAuthorEventsStreamSource, AuthorRepository authorRepository, FeedJobCheckpointer<Event> feedJobCheckpointer) {
        this.oldPlatformAuthorEventsStreamSource = oldPlatformAuthorEventsStreamSource;
        this.authorRepository = authorRepository;
        this.feedJobCheckpointer = feedJobCheckpointer;
    }

    void applyNewEvents() {
        feedJobCheckpointer.getLastCheckpoint()
                .map(oldPlatformAuthorEventsStreamSource::getEventsStreamStartingAfter)
                .orElseGet(oldPlatformAuthorEventsStreamSource::getEventsStream)
                .forEach(event -> {
                    updateAuthorWith(event);
                    feedJobCheckpointer.checkpoint(event);
                });
    }

    private void updateAuthorWith(Event event) {
        if ("REGISTERED".equals(event.getType())) {
            authorRepository.save(new Author(event.getEntityId(), event.getEntityVersion(), deserializeToEventData(event.getData()).getName(), false));
        } else {
            Author author = findAuthorForEvent(event);

            if (author == null) {
                LOGGER.warn(String.format("Skipping event %s as author with ID '%s' could not be found", event, event.getEntityId()));
                return;
            }

            if (applyEventTo(author, event)) {
                authorRepository.save(author);
            }
        }
    }

    private boolean applyEventTo(Author author, Event event) {
        try {
            author.apply(event);
            return true;
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            LOGGER.warn(String.format("Exception thrown whilst attempting to apply event: %s", e.getEvent()), e);
            return false;
        }
    }

    private Author findAuthorForEvent(Event event) {
        try {
            return authorRepository.getById(event.getEntityId());
        } catch (AuthorNotFoundException e) {
            return null;
        }
    }

}
