package com.ssrn.papers.replicator.postgres;

import com.ssrn.papers.replicator.FeedJobCheckpointer;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.util.StringColumnMapper;

import java.util.Optional;
import java.util.function.Function;

public class PostgresFeedJobCheckpointer<TFeedItem> implements FeedJobCheckpointer<TFeedItem> {
    private final DBI dbi;
    private final String checkpointJobName;
    private Optional<String> lastCheckpoint = Optional.empty();
    private final Function<TFeedItem, String> checkpointExtractor;

    public PostgresFeedJobCheckpointer(DBI dbi, String checkpointJobName, Function<TFeedItem, String> checkpointExtractor) {
        this.dbi = dbi;
        this.checkpointJobName = checkpointJobName;
        this.checkpointExtractor = checkpointExtractor;
    }

    public void checkpoint(TFeedItem feedItem) {
        String checkpoint = checkpointExtractor.apply(feedItem);

        dbi.withHandle(handle -> {
            handle.createStatement("INSERT INTO job_checkpoint (job, checkpoint) VALUES (:job_name, :checkpoint)" +
                    "ON CONFLICT (job) DO UPDATE SET checkpoint = EXCLUDED.checkpoint")
                    .bind("checkpoint", checkpoint)
                    .bind("job_name", checkpointJobName)
                    .execute();

            return null;
        });

        lastCheckpoint = Optional.of(checkpoint);
    }

    public Optional<String> getLastCheckpoint() {
        if (!lastCheckpoint.isPresent()) {
            lastCheckpoint = Optional.ofNullable(dbi.withHandle(handle -> handle.createQuery("SELECT checkpoint FROM job_checkpoint WHERE job = :job")
                    .bind("job", checkpointJobName)
                    .map(StringColumnMapper.INSTANCE)
                    .first()));
        }

        return lastCheckpoint;
    }
}
