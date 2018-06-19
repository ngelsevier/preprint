package com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.WebResource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;

public class PaginatedHttpEventStream extends WebResource {

    private final HttpClient httpClient;
    private String basicAuthenticationHeader;
    private String entrypointUrl;

    public PaginatedHttpEventStream(String relativeEntrypointUrl, String baseUrl, HttpClient httpClient, String basicAuthenticationHeader) {
        super(baseUrl);
        this.entrypointUrl = absoluteUrlWithPath(relativeEntrypointUrl);
        this.httpClient = httpClient;
        this.basicAuthenticationHeader = basicAuthenticationHeader;
    }

    public Event getNewestEvent() {
        return getStreamOfEventsWithNewestEventFirst(1).findFirst().get();
    }

    public List<Event> getNewEventsAfter(String previousEventId) {
        NewEventCollector newEventCollector = new NewEventCollector(previousEventId);

        getStreamOfEventsWithNewestEventFirst(100)
                .peek(newEventCollector::collect)
                .noneMatch(event -> newEventCollector.previousEventWasEncountered());

        return newEventCollector.getNewEvents();
    }

    public List<Event> getNewEventsAfter(String previousEventId, String entityId) {
        return getNewEventsAfter(previousEventId)
                .stream()
                .filter(event -> entityId.equals(event.getEntityId()))
                .collect(Collectors.toList());
    }

    private Stream<Event> getStreamOfEventsWithNewestEventFirst(int maximumEventPagesToRetrieve) {
        return Stream.iterate(getHalEventsPage(entrypointUrl), halEventsPage -> getHalEventsPage(halEventsPage.getPreviousPageUrl()))
                .limit(maximumEventPagesToRetrieve)
                .flatMap(halEventsPage -> {
                    Collections.reverse(halEventsPage.getEvents());
                    return halEventsPage.getEvents().stream();
                });
    }

    private HalEventsPage getHalEventsPage(String pageUrl) {
        Response response = httpClient.get(pageUrl,
                headers(
                        header("Accept", MediaType.APPLICATION_JSON),
                        header("Authorization", basicAuthenticationHeader)
                ));

        Response.StatusType statusInfo = response.getStatusInfo();
        Response.Status expectedStatus = Response.Status.OK;

        if (!statusInfo.equals(expectedStatus)) {
            throw new RuntimeException(String.format("Expected GET %s to respond with %s status but was %s", pageUrl, expectedStatus, statusInfo));
        }

        return response.readEntity(HalEventsPage.class);
    }

    private static class NewEventCollector {
        private final List<Event> newEvents = new ArrayList<>();
        private final String previousEventId;
        private boolean previousEventEncountered;

        public NewEventCollector(String previousEventId) {
            this.previousEventId = previousEventId;
        }

        public void collect(Event event) {
            if (previousEventEncountered) {
                return;
            }

            if (previousEventId.equals(event.getId())) {
                previousEventEncountered = true;
                return;
            }

            newEvents.add(event);
        }

        public List<Event> getNewEvents() {
            List<Event> events = newEvents.subList(0, newEvents.size());
            Collections.reverse(events);
            return newEvents;
        }

        public boolean previousEventWasEncountered() {
            return previousEventEncountered;
        }
    }
}
