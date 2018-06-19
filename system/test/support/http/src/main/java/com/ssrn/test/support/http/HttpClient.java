package com.ssrn.test.support.http;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class HttpClient {
    private final Client client;

    public static HttpClientConfiguration defaultConfiguration() {
        return new HttpClientConfiguration() {
            @Override
            public int connectionTimeoutMillisseconds() {
                return 5000;
            }

            @Override
            public int readTimeoutMilliseconds() {
                return 5000;
            }

            @Override
            public Level logLevel() {
                return Level.OFF;
            }

            @Override
            public Boolean logEntity() {
                return false;
            }

            @Override
            public int maxEntityBytesToLog() {
                return 8192;
            }
        };
    }

    public HttpClient(String name) {
        this(name, defaultConfiguration());
    }

    public HttpClient(String name, HttpClientConfiguration httpClientConfiguration) {
        Logger log = Logger.getLogger(name);
        log.setLevel(httpClientConfiguration.logLevel());

        int maxEntitySize = httpClientConfiguration.maxEntityBytesToLog();

        LoggingFeature loggingFeature = new LoggingFeature(
                log,
                Level.INFO,
                httpClientConfiguration.logEntity() ? LoggingFeature.Verbosity.PAYLOAD_ANY : LoggingFeature.Verbosity.HEADERS_ONLY,
                maxEntitySize);

        this.client = ClientBuilder.newClient()
                .register(JacksonJsonProvider.class)
                .property(ClientProperties.CONNECT_TIMEOUT, httpClientConfiguration.connectionTimeoutMillisseconds())
                .property(ClientProperties.READ_TIMEOUT, httpClientConfiguration.readTimeoutMilliseconds())
                .register(loggingFeature);
    }

    public static AbstractMap.SimpleEntry<String, Object> header(String name, Object value) {
        return new AbstractMap.SimpleEntry<>(name, value);
    }

    public static AbstractMap.SimpleEntry<String, List<Object>> queryParameter(String name, Object... values) {
        return new AbstractMap.SimpleEntry<>(name, asList(values));
    }

    @SafeVarargs
    public static HashMap<String, List<Object>> queryParameters(AbstractMap.SimpleEntry<String, List<Object>>... queryParameters) {
        HashMap<String, List<Object>> map = new HashMap<>();

        Arrays.stream(queryParameters).forEach(queryParameter -> map.put(queryParameter.getKey(), queryParameter.getValue()));

        return map;
    }

    @SafeVarargs
    public static HashMap<String, Object> headers(AbstractMap.SimpleEntry<String, Object>... headers) {
        HashMap<String, Object> map = new HashMap<>();

        Arrays.stream(headers).forEach(simpleEntry -> map.put(simpleEntry.getKey(), simpleEntry.getValue()));

        return map;
    }

    public String postAndExpect(Response.Status expectedStatus, String baseUri, String path) {
        return postAndExpect(expectedStatus, baseUri, path, Optional.empty(), String.class);
    }

    public String postAndExpect(Response.Status expectedStatus, String baseUri, String path, Object entity) {
        return postAndExpect(expectedStatus, baseUri, path, Optional.of(entity), String.class);
    }

    public <T> T postAndExpect(Response.Status expectedStatus, String baseUri, String path, Object entity, Class<T> responseEntityType) {
        return postAndExpect(expectedStatus, baseUri, path, Optional.of(entity), responseEntityType);
    }

    private <T> T postAndExpect(Response.Status expectedStatus, String baseUri, String path, Optional<Object> entity, Class<T> responseEntityType) {
        URI uri = createUri(baseUri, path);

        Response response = post(uri, entity);

        Response.StatusType actualStatus = response.getStatusInfo();

        if (actualStatus.getStatusCode() != expectedStatus.getStatusCode()) {
            throw new RuntimeException(String.format("Expected to receive '%s' status in response to POST %s but received '%s' status", expectedStatus, uri, actualStatus));
        }

        return response.readEntity(responseEntityType);
    }

    public Response getAndExpect(Response.Status expectedStatus, String baseUri, String path, HashMap<String, Object> headers) {
        return getAndExpect(expectedStatus, headers, queryParameters(), createUri(baseUri, path));
    }

    public Response getAndExpect(Response.Status expectedStatus, String url, HashMap<String, Object> headers, HashMap<String, List<Object>> queryParameters) {
        return getAndExpect(expectedStatus, headers, queryParameters, createUri(url, null));
    }

    public Response post(String baseUri, String path, Object entity) {
        return post(createUri(baseUri, path), Optional.of(entity));
    }

    public Response post(String baseUri, String path) {
        return post(createUri(baseUri, path), Optional.empty());
    }

    public Response get(String baseUri, String path) {
        return get(baseUri, path, headers(), queryParameters());
    }

    public Response get(String baseUri, HashMap<String, Object> headers) {
        return get(baseUri, null, headers, queryParameters());
    }

    public Response get(String baseUri, HashMap<String, Object> headers, HashMap<String, List<Object>> queryParameters) {
        return get(baseUri, null, headers, queryParameters);
    }

    public Response get(String baseUri, String path, HashMap<String, Object> headers) {
        return get(baseUri, path, headers, queryParameters());
    }


    public Response get(String baseUri, String path, HashMap<String, Object> headers, Map<String, List<Object>> queryParameters) {
        return get(createUri(baseUri, path), queryParameters, headers);
    }

    private Response get(URI uri, Map<String, List<Object>> queryParameters, HashMap<String, Object> headers) {
        WebTarget intialWebTarget = client.target(uri);

        WebTarget webTarget = queryParameters.entrySet()
                .stream()
                .reduce(intialWebTarget, (previousWebTarget, queryParameter) -> {
                    String parameterName = queryParameter.getKey();
                    List<Object> parameterValues = queryParameter.getValue();
                    return previousWebTarget.queryParam(parameterName, parameterValues.toArray());
                }, (neverCalled1, neverCalled2) -> null);

        return webTarget.request()
                .headers(new MultivaluedHashMap<>(headers))
                .get();
    }

    private Response post(URI uri, Optional<Object> entity) {
        Invocation.Builder request = client.target(uri).request();
        return entity
                .map(o -> request.method(HttpMethod.POST, Entity.json(o)))
                .orElseGet(() -> request.method(HttpMethod.POST));
    }

    private URI createUri(String baseUri, String path) {
        UriBuilder uriBuilder = UriBuilder.fromPath(baseUri);

        if (path != null) {
            uriBuilder.path(path);
        }

        return uriBuilder.build();
    }

    public Response putAndExpect(Response.Status expectedStatus, String baseUrl, String path, Entity text) {
        Response response = client.target(baseUrl).path(path).request().put(text);

        Response.StatusType actualStatus = response.getStatusInfo();

        if (actualStatus.getStatusCode() != expectedStatus.getStatusCode()) {
            throw new RuntimeException(String.format("Expected to receive '%s' status in response to POST %s but received '%s' status", expectedStatus, baseUrl, actualStatus));
        }

        return response;
    }

    private Response getAndExpect(Response.Status expectedStatus, HashMap<String, Object> headers, HashMap<String, List<Object>> queryParameters, URI uri) {
        Response response = get(uri, queryParameters, headers);
        Response.StatusType actualStatus = response.getStatusInfo();

        if (actualStatus.getStatusCode() != expectedStatus.getStatusCode()) {
            throw new RuntimeException(String.format("Expected to receive '%s' status in response to GET %s but received '%s' status", expectedStatus, uri, actualStatus));
        }

        return response;
    }
}
