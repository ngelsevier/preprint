package com.ssrn.papers.replicator.http_old_platform_paper_events_feed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {
    private final Links links;
    private List<Event> events;

    @JsonCreator
    public Page(@JsonProperty(value = "_links", required = true) Links links, @JsonProperty(value = "events", required = true) List<Event> events) {
        this.links = links;
        this.events = events;
    }

    Page cloneWithEventsSubsequentTo(String eventId) {
        AtomicBoolean eventWasEncountered = new AtomicBoolean(false);
        List<Event> subsequentEvents = events
                .stream()
                .peek(event -> eventWasEncountered.set((eventWasEncountered.get() || eventId.equals(event.getId()))))
                .filter(event -> eventWasEncountered.get() && !eventId.equals(event.getId()))
                .collect(Collectors.toList());

        return new Page(links, subsequentEvents);
    }

    List<Event> getEvents() {
        return events;
    }

    Links getLinks() {
        return links;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private Link previousArchive;
        private Link nextArchive;
        private Link via;
        private Link self;

        @JsonCreator
        public Links(@JsonProperty("prev-archive") Link previousArchive,
                     @JsonProperty("via") Link via,
                     @JsonProperty("next-archive") Link nextArchive,
                     @JsonProperty(value = "self", required = true) Link self) {
            this.previousArchive = previousArchive;
            this.via = via;
            this.nextArchive = nextArchive;

            this.self = self;
        }

        Link getPreviousArchive() {
            return previousArchive;
        }

        public Link getVia() {
            return via;
        }

        Link getNextArchive() {
            return nextArchive;
        }

        public Link getSelf() {
            return self;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Link {
            private final String href;

            @JsonCreator
            public Link(@JsonProperty(value = "href", required = true) String href) {
                this.href = href;
            }

            String getHref() {
                return href;
            }
        }
    }
}