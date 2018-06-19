package com.ssrn.papers.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.papers.domain.Paper;

import java.io.IOException;

public class JsonPaperDeserializer {
    private final ObjectMapper objectMapper;

    public JsonPaperDeserializer(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    Paper deserializePaperFromJson(String paperJson) {
        try {
            return objectMapper.readValue(paperJson, Paper.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
