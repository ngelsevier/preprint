package com.ssrn.authors.replicator.http_old_platform_author_events_feed;

import com.ssrn.authors.domain.Event;
import com.ssrn.authors.replicator.OldPlatformAuthorEventsStreamSource;
import com.ssrn.authors.replicator.page_item_stream.LinkedPageItemStreamFactory;

import javax.ws.rs.client.Client;
import java.util.logging.Level;
import java.util.stream.Stream;

public class EventsStreamSource implements OldPlatformAuthorEventsStreamSource {
    private final Feed authorEventsFeed;
    private final LinkedPageItemStreamFactory<Page, Event> linkedPageItemStreamFactory;

    public EventsStreamSource(String baseUrl, String basicAuthUsername, String basicAuthPassword, Client httpClient, int maxPageRequestRetries, Level httpRequestLogLevel) {
        PageSource pageSource = new PageSource(httpClient, basicAuthUsername, basicAuthPassword, maxPageRequestRetries, httpRequestLogLevel);
        linkedPageItemStreamFactory = new LinkedPageItemStreamFactory<>(
                page -> page.getEvents().stream().map(oldPlatformAuthorEvent -> new Event(
                                oldPlatformAuthorEvent.getId(),
                                oldPlatformAuthorEvent.getEntityId(),
                                oldPlatformAuthorEvent.getEntityVersion(),
                                oldPlatformAuthorEvent.getType(),
                                oldPlatformAuthorEvent.getDataJson(),
                                oldPlatformAuthorEvent.getEntityTimestamp()
                        )
                ),
                page -> page.getLinks().getNextArchive() == null,
                page -> pageSource.getPageAt(page.getLinks().getNextArchive().getHref())
        );
        authorEventsFeed = new Feed(pageSource, baseUrl);
    }

    @Override
    public Stream<Event> getEventsStream() {
        Page oldestPageInFeed = authorEventsFeed.seekOldestPageInFeed();
        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(oldestPageInFeed);
    }

    @Override
    public Stream<Event> getEventsStreamStartingAfter(String eventId) {
        Page pageContainingEvent = authorEventsFeed.seekPageContainingEvent(eventId);
        Page pageContainingSubsequentEvents = pageContainingEvent.cloneWithEventsSubsequentTo(eventId);

        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(pageContainingSubsequentEvents);
    }

}
