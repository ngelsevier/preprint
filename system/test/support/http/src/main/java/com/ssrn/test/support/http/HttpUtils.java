package com.ssrn.test.support.http;

import org.apache.http.client.utils.URIBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;

public class HttpUtils {
    public static String getQueryParameterValueIn(String url, String queryParameterName) {
        try {
            return new URIBuilder(url).getQueryParams().stream().filter(nameValuePair -> queryParameterName.equals(nameValuePair.getName())).findFirst().get().getValue();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAbsoluteUrl(String baseUrl, String path) {
        return UriBuilder.fromPath(baseUrl).path(path).toString();
    }
}
