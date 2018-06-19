package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.logging.Level;

public class NewSsrnWebsite {
    private final String baseUrl;
    private final SsrnWebsite ssrnWebsite;
    private final int pageLoadTimeoutSeconds;
    private HttpClient httpClient;
    private final String username = "username";
    private final String password = "password";
    private final String hostname = "frontend-website.internal-service";
    private final String basePath = "";

    public NewSsrnWebsite(SsrnWebsite ssrnWebsite, int pageLoadTimeoutSeconds) {
        this.baseUrl = String.format("http://%s:%s@%s%s", username, password, hostname, basePath);
        this.ssrnWebsite = ssrnWebsite;
        httpClient = createHttpClient();
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    public String absoluteUrlTo(String relativePath) {
        return String.format("http://%s%s%s", hostname, basePath, relativePath);
    }

    public SearchPage searchPage() {
        return new SearchPage(baseUrl, ssrnWebsite, pageLoadTimeoutSeconds);
    }

    public boolean isAvailable() {
        try {
            return Response.Status.OK.getStatusCode() == httpClient.get(baseUrl, "/healthcheck").getStatusInfo().getStatusCode();
        } catch (ProcessingException e) {
            return false;
        }
    }

    private static HttpClient createHttpClient() {
        return new HttpClient("New SSRN Website", new HttpClientConfiguration() {
            @Override
            public int connectionTimeoutMillisseconds() {
                return 500;
            }

            @Override
            public int readTimeoutMilliseconds() {
                return 500;
            }

            @Override
            public Level logLevel() {
                return Level.INFO;
            }

            @Override
            public Boolean logEntity() {
                return false;
            }

            @Override
            public int maxEntityBytesToLog() {
                return 0;
            }
        });
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
