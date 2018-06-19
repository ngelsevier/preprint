package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class AuthorEvent extends Event{
    @JsonCreator()
    public AuthorEvent(@JsonProperty(value = "id", required = true) String id,
                       @JsonProperty(value = "type", required = true) String type,
                       @JsonProperty(value = "entityId", required = true) String entityId,
                       @JsonProperty(value = "timestamp", required = true) String timestamp,
                       @JsonProperty(value = "entityVersion", required = true) int entityVersion,
                       @JsonProperty("data") AuthorEventData data
    ) {
        super(id, type, entityId, timestamp, entityVersion, data);
    }

    public AuthorEvent(String id, String type, String entityId, DateTime timestamp, int entityVersion, AuthorEventData data) {
        this(id, type, entityId, timestamp.toString(ISODateTimeFormat.dateTime()), entityVersion, data);
    }
}
