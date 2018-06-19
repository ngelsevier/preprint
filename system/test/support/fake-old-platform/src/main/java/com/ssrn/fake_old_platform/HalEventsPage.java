package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HalEventsPage {
    private final String requestedUrl;
    private Map<String, Map<String, Object>> links = new HashMap<>();
    private EventLogPage eventLogPage;
    private final String baseUrl;

    HalEventsPage(EventLogPage eventLogPage, String requestedUrl, String baseUrl) {
        this.eventLogPage = eventLogPage;
        this.requestedUrl = requestedUrl;
        this.baseUrl = baseUrl;
    }

    @JsonProperty("_links")
    public Map<String, Map<String, Object>> getLinks() {
        addHypermediaLinkTo(links, "self", requestedUrl);
        addHypermediaLinkTo(links, "via", String.format("%s/%s", baseUrl, eventLogPage.getPageId()));

        if (eventLogPage.getPreviousPageId() != null) {
            addHypermediaLinkTo(links, "prev-archive", String.format("%s/%s", baseUrl, eventLogPage.getPreviousPageId()));
        }

        if (eventLogPage.getNextPageId() != null) {
            addHypermediaLinkTo(links, "next-archive", String.format("%s/%s", baseUrl, eventLogPage.getNextPageId()));
        }

        return links;
    }

    private static void addHypermediaLinkTo(Map<String, Map<String, Object>> links, String linkName, final String href) {
        links.put(linkName, new HashMap<String, Object>() {
            {
                put("href", href);
            }
        });
    }

    public List<Event> getEvents() {
        return eventLogPage.getEvents();
    }

}
