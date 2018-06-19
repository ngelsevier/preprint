package com.ssrn.authors.replicator.http_old_platform_author_events_feed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private final String entityId;
    private final String type;
    private String dataJson;
    private String id;
    private int entityVersion;
    private DateTime entityTimestamp;

    @JsonCreator
    public Event(
            @JsonProperty(value = "id") String id,
            @JsonProperty(value = "entity_id") String entityId,
            @JsonProperty(value = "type") String type,
            @JsonProperty(value = "data") ObjectNode dataJson,
            @JsonProperty(value = "entity_version") int entityVersion,
            @JsonProperty(value = "timestamp") String timestamp) {
        this.id = id;
        this.entityId = entityId;
        this.type = type;
        this.dataJson = dataJson == null ? null : dataJson.toString();
        this.entityVersion = entityVersion;
        this.entityTimestamp = DateTime.parse(timestamp);
    }

    public String getId() {
        return id;
    }

    public String getDataJson() {
        return dataJson;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getType() {
        return type;
    }

    public int getEntityVersion() {
        return entityVersion;
    }

    public DateTime getEntityTimestamp() {
        return entityTimestamp;
    }
}
