package com.ssrn.authors.shared.test_support.event;

import com.ssrn.authors.domain.Event;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.UUID;

public class EventBuilder {
    private String id = UUID.randomUUID().toString();

    private String entityId = UUID.randomUUID().toString();
    private int entityVersion = 1;
    private String type = "DRAFTED";
    private String data;
    private DateTime entityTimestamp = DateTime.now();

    public static EventBuilder anEvent() {
        return new EventBuilder();
    }

    public static EventBuilder anAuthorRegisteredEvent() {
        return anEvent().withType("REGISTERED").withData(new JSONObject().put("name", "Author Name"));
    }

    private EventBuilder() {
    }

    public EventBuilder withData(JSONObject data) {
        this.data = JSONObject.valueToString(data);
        return this;
    }

    public EventBuilder withData(String data) {
        this.data = data;
        return this;
    }

    public EventBuilder withNoData() {
        this.data = null;
        return this;
    }

    public EventBuilder withEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public EventBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public EventBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public EventBuilder withEntityVersion(int entityVersion) {
        this.entityVersion = entityVersion;
        return this;
    }

    public EventBuilder withEntityTimestamp(DateTime entityTimestamp) {
        this.entityTimestamp = entityTimestamp;
        return this;
    }

    public Event build() {
        return new Event(id, entityId, entityVersion, type, data, entityTimestamp);
    }
}
