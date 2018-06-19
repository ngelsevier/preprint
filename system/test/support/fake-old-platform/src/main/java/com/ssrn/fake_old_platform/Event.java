package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class Event {
    final String entityId;
    final String id;
    final String type;
    final String timestamp;
    final int entityVersion;
    final EventData data;

    public Event(String id, String type, String entityId, String timestamp, int entityVersion, EventData data) {
        this.id = id;
        this.type = type;
        this.entityId = entityId;
        this.timestamp = timestamp;
        this.entityVersion = entityVersion;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getEntityVersion() {
        return entityVersion;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public EventData getData() {
        return data;
    }
}
