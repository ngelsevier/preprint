package com.ssrn.test.support.old_platform_contract_test.ssrn.api.eventfeed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private final String type;
    private final String id;
    private String entityId;
    private int entityVersion;
    private String dataJson;
    private String timestamp;

    @JsonCreator
    public Event(@JsonProperty(value = "id", required = true) String id,
                 @JsonProperty(value = "type", required = true) String type,
                 @JsonProperty(value = "entityId", required = true) String entityId,
                 @JsonProperty(value = "entityVersion", required = true) int entityVersion,
                 @JsonProperty(value = "data") ObjectNode dataJson,
                 @JsonProperty(value = "timestamp") String timestamp) {
        this.type = type;
        this.id = id;
        this.entityId = entityId;
        this.entityVersion = entityVersion;
        this.dataJson = dataJson == null ? null : dataJson.toString();
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getEntityId() {
        return entityId;
    }

    public int getEntityVersion() {
        return entityVersion;
    }

    public String getDataJson() {
        return dataJson;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", entityId='" + entityId + '\'' +
                ", entityVersion=" + entityVersion +
                ", dataJson='" + dataJson + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (entityVersion != event.entityVersion) return false;
        if (!type.equals(event.type)) return false;
        if (!id.equals(event.id)) return false;
        if (!entityId.equals(event.entityId)) return false;
        if (dataJson != null ? !dataJson.equals(event.dataJson) : event.dataJson != null) return false;

        return timestamp.equals(event.timestamp);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + entityId.hashCode();
        result = 31 * result + entityVersion;
        result = 31 * result + (dataJson != null ? dataJson.hashCode() : 0);
        result = 31 * result + timestamp.hashCode();
        return result;
    }
}
