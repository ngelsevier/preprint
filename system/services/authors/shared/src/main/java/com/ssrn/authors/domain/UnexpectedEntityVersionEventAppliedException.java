package com.ssrn.authors.domain;

public class UnexpectedEntityVersionEventAppliedException extends RuntimeException {
    private final Event event;
    private int currentVersion;

    UnexpectedEntityVersionEventAppliedException(Event event, int currentVersion) {
        super(String.format("Could not apply event with entity version %d to entity with version %d",
                event.getEntityVersion(),
                currentVersion)
        );
        this.event = event;
        this.currentVersion = currentVersion;
    }

    public Event getEvent() {
        return event;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }
}
