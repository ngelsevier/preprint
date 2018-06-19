package com.ssrn.authors.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.authors.domain.Author;

import java.io.IOException;

public class JsonAuthorDeserializer {
    private final ObjectMapper objectMapper;

    public JsonAuthorDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Author deserializeAuthorFromJson(String authorJson) {
        try {
            return objectMapper.readValue(authorJson, Author.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
