package com.ssrn.test.support.ssrn.api;

import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;
import com.ssrn.test.support.http.HttpUtils;
import com.ssrn.test.support.ssrn.SsrnConfiguration;

import javax.ws.rs.core.MediaType;
import java.util.Base64;

import static com.ssrn.test.support.http.HttpClient.header;

public class SsrnOldPlatformApiTest {

    private final SsrnTestDataClient ssrnTestDataClient;
    private final HttpClientConfiguration httpClientConfiguration;
    private final SsrnConfiguration ssrnConfiguration;
    private HttpClient httpClient;

    public SsrnOldPlatformApiTest(HttpClientConfiguration httpClientConfiguration, SsrnConfiguration ssrnConfiguration) {
        this.httpClientConfiguration = httpClientConfiguration;
        this.ssrnConfiguration = ssrnConfiguration;
        ssrnTestDataClient = new SsrnTestDataClient(
                ssrnAbsoluteUrl("/rest/meta/resetParticipant"),
                header("Authorization", ssrnBasicAuthenticationHeader()),
                header("Accept", MediaType.APPLICATION_JSON),
                httpClient()
        );
    }

    protected HttpClient httpClient() {
        if (httpClient == null) {
            httpClient = new HttpClient("SSRN API HTTP Client", httpClientConfiguration);
        }

        return httpClient;
    }

    protected String ssrnAbsoluteUrl(String path) {
        return HttpUtils.getAbsoluteUrl(ssrnConfiguration.baseUrl(), path);
    }

    protected String ssrnBasicAuthenticationHeader() {
        return base64EncodedBasicAuthorizationHeader(
                ssrnConfiguration.getAuthenticationUsername(),
                ssrnConfiguration.getAuthenticationPassword()
        );
    }

    protected SsrnTestDataClient ssrnTestDataClient() {
        return ssrnTestDataClient;
    }

    protected String base64EncodedBasicAuthorizationHeader(String username, String password) {
        return String.format("Basic %s", new String(Base64.getEncoder().encode((String.format("%s:%s", username, password)).getBytes())));
    }
}
