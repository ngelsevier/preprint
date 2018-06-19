package com.ssrn.papers.replicator;

import java.util.Optional;

public interface FeedJobCheckpointer<TFeedItem> {
    void checkpoint(TFeedItem feedItem);

    Optional<String> getLastCheckpoint();
}
