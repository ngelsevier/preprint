package com.ssrn.authors.replicator;

import com.ssrn.authors.domain.Event;
import java.util.stream.Stream;

public interface OldPlatformAuthorEventsStreamSource {
    Stream<Event> getEventsStream();

    Stream<Event> getEventsStreamStartingAfter(String latestEventId);
}
