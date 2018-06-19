package com.ssrn.frontend.website;

import com.ssrn.frontend.website.search.SearchApiSearchEngine;
import com.ssrn.frontend.website.search.SearchPage;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ssrn.shared.kms.KmsUtils.usingKmsDecrypt;

public class Service extends Application<ServiceConfiguration> {

    private static final boolean SIMULATED_ENVIRONMENT = Boolean.parseBoolean(System.getenv("SIMULATED_ENVIRONMENT"));
    private static final String BASIC_AUTH_USERNAME = getDecryptedCredential("username", "ENCRYPTED_FRONTEND_WEBSITE_USERNAME");
    private static final String BASIC_AUTH_PASSWORD = getDecryptedCredential("password", "ENCRYPTED_FRONTEND_WEBSITE_PASSWORD");
    private static final int SEARCH_RESULT_PAGE_SIZE = Integer.parseInt(System.getenv("SEARCH_RESULT_PAGE_SIZE"));

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(ServiceConfiguration configuration, Environment environment) throws Exception {
        Client searchApiClient = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClient())
                .build("Search API")
                .register(new LoggingFeature(Logger.getLogger(JerseyClient.class.getName()),
                        Level.parse(getEnvironmentVariable("HTTP_REQUEST_LOG_LEVEL", "INFO")), LoggingFeature.Verbosity.HEADERS_ONLY, 8192));

        SearchApiSearchEngine searchApiSearchEngine = new SearchApiSearchEngine(getSearchServiceBaseUrl(), searchApiClient, SEARCH_RESULT_PAGE_SIZE);
        environment.jersey().register(new SearchPage(
                searchApiSearchEngine,
                System.getenv("OLD_PLATFORM_ARTICLE_PAGE_BASE_URL"),
                System.getenv("OLD_PLATFORM_AUTHOR_PROFILE_PAGE_BASE_URL"),
                System.getenv("OLD_PLATFORM_AUTHOR_IMAGE_BASE_URL"),
                System.getenv("OLD_PLATFORM_AUTH_BASE_URL"), SEARCH_RESULT_PAGE_SIZE));

        environment.jersey().register(new HealthcheckResource());

        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(Service::getUserByCredentials)
                .buildAuthFilter()));

        environment.jersey().register(new UnhandledExceptionMapper());
        environment.servlets().addFilter("HttpCacheControlFilter", new HttpCacheControlFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
    }

    private static Optional<User> getUserByCredentials(BasicCredentials credentials) {
        return BASIC_AUTH_USERNAME.equals(credentials.getUsername()) && BASIC_AUTH_PASSWORD.equals(credentials.getPassword()) ?
                Optional.of(new User(credentials.getUsername())) :
                Optional.empty();
    }

    private static String getDecryptedCredential(String defaultValue, String environmentKey) {
        return SIMULATED_ENVIRONMENT ? defaultValue : usingKmsDecrypt(System.getenv(environmentKey));
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());

        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "css"));
        bootstrap.addBundle(new AssetsBundle("/assets/js", "/js", null, "js"));
        bootstrap.addBundle(new AssetsBundle("/assets/fonts", "/fonts", null, "fonts"));
        bootstrap.addBundle(new AssetsBundle("/assets/images", "/images", null, "images"));
    }

    private static String getSearchServiceBaseUrl() {
        String overriddenSearchServiceBaseUrl = System.getenv("SEARCH_SERVICE_BASE_URL");
        return overriddenSearchServiceBaseUrl == null ? "http://search.internal-service" : overriddenSearchServiceBaseUrl;
    }

    private static String getEnvironmentVariable(String name, String defaultValue) {
        return System.getenv(name) == null ? defaultValue : System.getenv(name);
    }

}
