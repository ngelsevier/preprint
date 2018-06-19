package com.ssrn.fake_old_platform.functional_tests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HalEventsPage {
    private final Links links;
    private List<Event> events;

    @JsonCreator
    public HalEventsPage(@JsonProperty(value = "_links", required = true) Links links, @JsonProperty(value = "events", required = true) List<Event> events) {
        this.links = links;
        this.events = events;
    }

    public List<Event> getEvents() {
        return events;
    }

    public String getPreviousPageUrl() {
        if (links.getPreviousArchive() == null) {
            throw new RuntimeException("No previous page URL defined");
        }

        return links.getPreviousArchive().getHref();
    }

    public Links getLinks() {
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

        public Link getPreviousArchive() {
            return previousArchive;
        }

        public Link getVia() {
            return via;
        }

        public Link getNextArchive() {
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

            public String getHref() {
                return href;
            }
        }
    }
}
