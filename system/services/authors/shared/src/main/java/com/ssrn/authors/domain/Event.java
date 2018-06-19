package com.ssrn.authors.domain;

import org.joda.time.DateTime;

public class Event {
    private final String id;
    private final String type;
    private final String data;
    private final String entityId;
    private final int entityVersion;
    private final DateTime entityTimestamp;

    public Event(String id, String entityId, int entityVersion, String type, String data, DateTime entityTimestamp) {
        this.id = id;
        this.entityId = entityId;
        this.entityVersion = entityVersion;
        this.type = type;
        this.data = data;
        this.entityTimestamp = entityTimestamp;
    }

    public String getId() {
        return id;
    }

    public String getEntityId() {
        return entityId;
    }

    public int getEntityVersion() { return entityVersion; }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public DateTime getEntityTimestamp() {
        return entityTimestamp;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", entityId='" + entityId + '\'' +
                ", entityVersion=" + entityVersion +
                ", entityTimestamp=" + entityTimestamp +
                '}';
    }
}
