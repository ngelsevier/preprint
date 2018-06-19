package com.ssrn.search.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONAware;
import net.minidev.json.JSONObject;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

class ElasticsearchClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean initialised = false;

    ElasticsearchClient(String nodeHostName, int nodePort) {
        this.restClient = RestClient.builder(new HttpHost(nodeHostName, nodePort, "http")).build();
    }

    <T> Response makeRequest(String httpMethod, String path, T requestEntity, Integer... expectedResponseCodes) {
        String requestBody = serializeToJson(objectMapper, requestEntity);
        return makeRequest(httpMethod, path, ContentType.APPLICATION_JSON, requestBody, expectedResponseCodes);
    }

    <T extends ElasticSearchDocument> void makeBulkIndexRequest(String httpMethod, String path, List<T> documents) {
        if (documents.size() > 0) {
            String requestBody = documents.stream().map(this::createBulkRequestInstructionToIndex).collect(Collectors.joining("\n")) + "\n";
            Response response = makeRequest(httpMethod, path, ContentType.create("application/x-ndjson", Charset.forName("UTF-8")), requestBody, 200);
            logAnyBulkRequestErrorsIn(response.getBody());
        }
    }

    void makeBulkDeleteRequest(String httpMethod, String path, List<String> documentIds) {
        if (documentIds.size() > 0) {
            String requestBody = documentIds.stream().map(this::createBulkRequestInstructionToDelete).collect(Collectors.joining("\n")) + "\n";
            Response response = makeRequest(httpMethod, path, ContentType.create("application/x-ndjson", Charset.forName("UTF-8")), requestBody, 200);
            logAnyBulkRequestErrorsIn(response.getBody());
        }
    }

    public Response makeRequest(String httpMethod, String path, ContentType contentType, String requestBody, Integer... expectedResponseCodes) {
        ensureInitialised();

        List<Integer> expectedStatusCodesList = Arrays.asList(expectedResponseCodes);

        org.elasticsearch.client.Response response;

        try {
            response = restClient.performRequest(httpMethod, path, Collections.emptyMap(), new NStringEntity(requestBody, contentType));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int actualResponseCode = response.getStatusLine().getStatusCode();

        if (!expectedStatusCodesList.contains(actualResponseCode)) {
            throw new RuntimeException(String.format("Expected response code %s but received %d",
                    expectedStatusCodesList.stream().map(integer -> Integer.toString(integer)).collect(Collectors.joining(" or ")),
                    actualResponseCode));
        }

        return new Response(response);
    }

    private <T extends ElasticSearchDocument> String createBulkRequestInstructionToIndex(T document) {
        return String.format("%s\n%s", createBulkRequestMetadata("index", document.getId()), serializeToJson(objectMapper, document));
    }

    private String createBulkRequestInstructionToDelete(String documentId) {
        return String.format("%s", createBulkRequestMetadata("delete", documentId));
    }

    private void ensureInitialised() {
        if (!initialised) {
            LOGGER.info("Initialising...");

            try {
                org.elasticsearch.client.Response response = checkIndexExists("/");
                long endTime = System.currentTimeMillis() + 30 * 1000;
                while (response.getStatusLine().getStatusCode() != 200 && System.currentTimeMillis() < endTime) {
                    LOGGER.info("Elasticsearch is not available, retrying shortly...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    response = checkIndexExists("/");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            initialised = true;
            LOGGER.info("Initialised");
        }
    }

    private org.elasticsearch.client.Response checkIndexExists(String endpoint) throws IOException {
        return restClient.performRequest("HEAD", endpoint, singletonMap("ignore", "400"));
    }

    private static <T> String serializeToJson(ObjectMapper objectMapper, T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String createBulkRequestMetadata(final String instruction, final String documentId) {
        return serializeToJson(objectMapper, new HashMap<String, Object>() {
            {
                put(instruction, new HashMap<String, Object>() {
                    {
                        put("_id", documentId);
                    }
                });
            }
        });
    }

    private static void logAnyBulkRequestErrorsIn(String bulkRequestResponseBody) {
        if (bulkRequestResponseBody != null && !bulkRequestResponseBody.equals("")) {
            DocumentContext documentContext = JsonPath.parse(bulkRequestResponseBody);

            if (documentContext.read("$.errors", Boolean.class)) {
                JSONObject[] itemsWithErrors = documentContext.read("$.items[*][?(@[*].error)]", JSONObject[].class);

                String itemsWithErrorsFormattedString = Arrays.stream(itemsWithErrors)
                        .map(JSONAware::toJSONString)
                        .collect(Collectors.joining("\n\n"));

                LOGGER.warn(String.format("Some actions failed in a bulk Elasticsearch request. The response included the following items with errors:\n\n%s", itemsWithErrorsFormattedString));
            }
        }
    }

    public static class Response {


        private final org.elasticsearch.client.Response response;

        Response(org.elasticsearch.client.Response response) {
            this.response = response;
        }

        String getBody() {
            try {
                return EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
