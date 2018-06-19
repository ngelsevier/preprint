package com.ssrn.fake_old_platform;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

class EventLog {
    private final List<EventLogPage> pagesList = new CopyOnWriteArrayList<>();
    private final int eventsPerPage;

    EventLog(int eventsPerPage, String oldestPageId) {
        this.eventsPerPage = eventsPerPage;
        pagesList.clear();
        pagesList.add(new EventLogPage(oldestPageId, null, null));
    }

    EventLogPage getPage(String pageId) {
        return pagesList
                .stream()
                .filter(eventLogPage -> pageId.equals(eventLogPage.getPageId()))
                .findFirst()
                .orElse(null);
    }

    EventLogPage getNewestPage() {
        return pagesList
                .stream()
                .filter(eventLogPage -> eventLogPage.getNextPageId() == null)
                .findFirst()
                .get();
    }

    List<Event> getListOfEventsContainingEntityId(String entityId) {
        return pagesList
                .stream()
                .flatMap(eventLogPage -> eventLogPage.getEvents().stream())
                .filter(eventMap -> entityId.equals(eventMap.getEntityId()))
                .collect(Collectors.toList());
    }

    void append(Event newEvent) {
        synchronized (pagesList) {
            EventLogPage currentNewestPage = getNewestPage();

            if (currentNewestPage.getEvents().size() < eventsPerPage) {
                currentNewestPage.append(newEvent);
                return;
            }

            EventLogPage newestPage = new EventLogPage(
                    UUID.randomUUID().toString(),
                    currentNewestPage.getPageId(),
                    null);
            newestPage.append(newEvent);

            pagesList.remove(currentNewestPage);
            pagesList.add(currentNewestPage.cloneWithNextPageId(newestPage.getPageId()));
            pagesList.add(newestPage);
        }
    }

}
