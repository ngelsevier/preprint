package com.ssrn.fake_old_platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class EventLogPage {
    private final List<Event> events;
    private final String pageId;
    private final String previousPageId;
    private final String nextPageId;

    EventLogPage(String pageId, String previousPageId, String nextPageId) {
        this(pageId, previousPageId, nextPageId, new CopyOnWriteArrayList<>());
    }

    private EventLogPage(String pageId, String previousPageId, String nextPageId, List<Event> events) {
        this.pageId = pageId;
        this.previousPageId = previousPageId;
        this.nextPageId = nextPageId;
        this.events = events;
    }

    String getPageId() {
        return pageId;
    }

    String getNextPageId() {
        return nextPageId;
    }

    String getPreviousPageId() {
        return previousPageId;
    }

    void append(Event event) {
        events.add(event);
    }

    List<Event> getEvents() {
        return events.subList(0, events.size());
    }

    EventLogPage cloneWithNextPageId(String nextPageId) {
        return new EventLogPage(pageId, previousPageId, nextPageId, getEvents());
    }
}
