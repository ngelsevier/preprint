package com.ssrn.papers.replicator.http_old_platform_paper_events_feed;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.replicator.OldPlatformPaperEventsStreamSource;
import com.ssrn.papers.replicator.page_item_stream.LinkedPageItemStreamFactory;

import javax.ws.rs.client.Client;
import java.util.logging.Level;
import java.util.stream.Stream;

public class EventsStreamSource implements OldPlatformPaperEventsStreamSource {
    private final Feed paperEventsFeed;
    private final LinkedPageItemStreamFactory<Page, Paper.Event> linkedPageItemStreamFactory;
    private final EventFactory eventFactory = new EventFactory();

    public EventsStreamSource(String baseUrl, String basicAuthUsername, String basicAuthPassword, Client httpClient, int maxPageRequestRetries, Level httpRequestLogLevel) {
        PageSource pageSource = new PageSource(httpClient, basicAuthUsername, basicAuthPassword, maxPageRequestRetries, httpRequestLogLevel);
        linkedPageItemStreamFactory = new LinkedPageItemStreamFactory<>(
                page -> page.getEvents().stream().map(eventFactory::map),
                page -> page.getLinks().getNextArchive() == null,
                page -> pageSource.getPageAt(page.getLinks().getNextArchive().getHref())
        );
        paperEventsFeed = new Feed(pageSource, baseUrl);
    }

    @Override
    public Stream<Paper.Event> getEventsStream() {
        Page oldestPageInFeed = paperEventsFeed.seekOldestPageInFeed();
        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(oldestPageInFeed);
    }

    @Override
    public Stream<Paper.Event> getEventsStreamStartingAfter(String eventId) {
        Page pageContainingEvent = paperEventsFeed.seekPageContainingEvent(eventId);
        Page pageContainingSubsequentEvents = pageContainingEvent.cloneWithEventsSubsequentTo(eventId);

        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(pageContainingSubsequentEvents);
    }

}
