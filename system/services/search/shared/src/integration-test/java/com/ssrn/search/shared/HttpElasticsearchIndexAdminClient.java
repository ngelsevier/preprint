package com.ssrn.search.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class HttpElasticsearchIndexAdminClient {

    private final RestClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    HttpElasticsearchIndexAdminClient(String nodeHostname, int nodePort) {
        client = RestClient.builder(new HttpHost(nodeHostname, nodePort, "http")).build();
    }

    void createIndex(String indexName) {
        HashMap<String, Object> indexConfiguration = new HashMap<>();
        makeRequest("PUT", indexName, indexConfiguration, 200);
    }

    void deleteIndex(String indexName) {
        makeRequest("DELETE", indexName, null, 200);
    }

    boolean doesIndexExist(String indexName) {
        Response response = makeRequest("HEAD", indexName, null, 200, 404);
        return response.getStatusLine().getStatusCode() == 200;
    }

    public Boolean isElasticsearchAvailable() {
        try {
            makeRequest("GET", "/", null, 200);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private <T> org.elasticsearch.client.Response makeRequest(String httpMethod, String path, T requestEntity, Integer... expectedResponseCodes) {
        List<Integer> expectedStatusCodesList = Arrays.asList(expectedResponseCodes);

        org.elasticsearch.client.Response response;

        try {
            response = requestEntity == null ?
                    client.performRequest(httpMethod, path) :
                    client.performRequest(httpMethod, path, Collections.emptyMap(), new NStringEntity(serializeToJson(objectMapper, requestEntity), ContentType.APPLICATION_JSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int actualResponseCode = response.getStatusLine().getStatusCode();

        if (!expectedStatusCodesList.contains(actualResponseCode)) {
            throw new RuntimeException(String.format("Expected response code %s but received %d",
                    expectedStatusCodesList.stream().map(integer -> Integer.toString(integer)).collect(Collectors.joining(" or ")),
                    actualResponseCode));
        }

        return response;
    }

    private static <T> String serializeToJson(ObjectMapper objectMapper, T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
