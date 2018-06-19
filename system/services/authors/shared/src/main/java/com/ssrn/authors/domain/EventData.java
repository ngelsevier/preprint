package com.ssrn.authors.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class EventData {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String name;

    @JsonCreator
    public EventData(@JsonProperty("name") String name) {
        this.name = name;
    }

    public static EventData deserializeToEventData(String eventData) {
        try {
            return objectMapper.readValue(eventData, EventData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }
}
