package com.ssrn.papers.replicator.http_old_platform_paper_events_feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.SubmissionStage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.ssrn.papers.domain.Paper.*;
import static com.ssrn.papers.domain.SubmissionStage.fromString;

public class EventFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Paper.Event map(Event event) {
        if (event.getType().equals("DRAFTED")) {
            boolean paperPrivate = EventFactory.<Boolean>dataPropertyIn(event, "isPrivate");
            boolean paperIrrelevant = EventFactory.<Boolean>dataPropertyIn(event, "isConsideredIrrelevant");
            boolean paperRestricted = EventFactory.<Boolean>dataPropertyIn(event, "isRestricted");
            boolean paperTolerated = EventFactory.<Boolean>dataPropertyIn(event, "isTolerated");

            Integer titleAsInt;
            String title;
            try {
                titleAsInt = EventFactory.<Integer>dataPropertyIn(event, "title");
                title = titleAsInt.toString();
            } catch (ClassCastException e) {
                title = EventFactory.dataPropertyIn(event, "title");
            }
            List<Integer> integerAuthorIds = EventFactory.dataPropertyIn(event, "authorIds");
            String[] authorIds = integerAuthorIds.stream().map(String::valueOf).toArray(String[]::new);

            return new DraftedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp(), title, authorIds, paperPrivate, paperIrrelevant, paperRestricted, paperTolerated);
        }

        if (event.getType().equals("TITLE CHANGED")) {
            String title = dataPropertyIn(event, "title").toString();
            return new TitleChangedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp(), title);
        }

        if (event.getType().equals("KEYWORDS CHANGED")) {
            String keywords = dataPropertyIn(event, "keywords").toString();
            return new KeywordsChangedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp(), keywords);
        }

        if (event.getType().equals("AUTHOR CHANGED")) {
            List<Integer> integerAuthorIds = EventFactory.dataPropertyIn(event, "authorIds");
            String[] authorIds = integerAuthorIds.stream().map(String::valueOf).toArray(String[]::new);
            return new AuthorChangedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp(), authorIds);
        }

        if (event.getType().equals("MADE PRIVATE")) {
            return new MadePrivateEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
        }

        if (event.getType().equals("MADE PUBLIC")) {
            return new MadePublicEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
        }

        if (event.getType().equals("CONSIDERED IRRELEVANT")) {
            return new ConsideredIrrelevantEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
        }

        if (event.getType().equals("CONSIDERED RELEVANT")) {
            return new ConsideredRelevantEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
        }

        if (event.getType().equals("RESTRICTED")) {
            return new RestrictedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
        }

        if (event.getType().equals("UNRESTRICTED")) {
            return new UnrestrictedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
        }

        if (event.getType().equals("SUBMISSION STAGE CHANGED")) {
            String jsonSubmissionStage = dataPropertyIn(event, "submissionStage");
            SubmissionStage submissionStage = fromString(jsonSubmissionStage);
            return new SubmissionStageChangedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp(), submissionStage);
        }

        if (event.getType().equals("DELETED")) {
            // DELETED events are intentionally mapped to SUBMISSION STAGE CHANGED to DELETED events
            // We have not yet implemented the logic to handle DELETEs in the Papers database
            // and Papers rather than PaperUpdates are streamed through Kinesis
            return new SubmissionStageChangedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp(), SubmissionStage.DELETED);
        }

        return new UnrecognisedEvent(event.getId(), event.getEntityId(), event.getEntityVersion(), event.getEntityTimestamp());
    }

    @SuppressWarnings("unchecked")
    private static <T> T dataPropertyIn(Event event, String name) {
        return (T) deserializeToEventData(event.getDataJson()).get(name);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deserializeToEventData(String eventData) {
        try {
            return (Map<String, Object>) objectMapper.readValue(eventData, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
