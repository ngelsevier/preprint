package com.ssrn.papers.replicator;

import com.ssrn.papers.domain.Paper;

import java.util.stream.Stream;

public interface OldPlatformPaperEventsStreamSource {
    Stream<Paper.Event> getEventsStream();

    Stream<Paper.Event> getEventsStreamStartingAfter(String latestEventId);
}
