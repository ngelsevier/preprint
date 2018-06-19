package com.ssrn.authors.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

import static com.ssrn.authors.domain.EventData.deserializeToEventData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {
    private static final String EVENT_UNREGISTERED = "UNREGISTERED";
    private static final List<String> EVENT_TYPES_THAT_UPDATE_NAME = Arrays.asList("REGISTERED", "NAME CHANGED");

    private final String id;
    private String name;
    private int version;
    private boolean removed;

    @JsonCreator
    public Author(@JsonProperty(value = "id", required = true) String id,
                  @JsonProperty(value = "version", required = true) int version,
                  @JsonProperty(value = "name", required = true) String name,
                  @JsonProperty(value = "removed", required = true) boolean removed) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.removed = removed;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isRemoved() {
        return removed;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                '}';
    }

    public int getVersion() {
        return version;
    }

    public void apply(Event event) {
        int currentVersion = getVersion();

        if (event.getEntityVersion() != currentVersion + 1) {
            throw new UnexpectedEntityVersionEventAppliedException(event, currentVersion);
        }

        if (EVENT_TYPES_THAT_UPDATE_NAME.contains(event.getType())) {
            name = deserializeToEventData(event.getData()).getName();
        }

        if (EVENT_UNREGISTERED.equals(event.getType())) {
            removed = true;
        }

        this.version = event.getEntityVersion();
    }

}