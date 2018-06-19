package com.ssrn.frontend.website;

import io.dropwizard.views.View;

import java.nio.charset.StandardCharsets;

public class BasePageView extends View {
    private final String authBaseUrl;

    public BasePageView(String templateName, String authBaseUrl) {
        super(templateName, StandardCharsets.UTF_8);
        this.authBaseUrl = authBaseUrl;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }
}
