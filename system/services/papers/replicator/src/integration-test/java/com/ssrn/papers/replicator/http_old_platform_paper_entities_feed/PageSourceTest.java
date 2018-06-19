package com.ssrn.papers.replicator.http_old_platform_paper_entities_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Service;
import com.ssrn.papers.replicator.RequestRetryLimitedExceeded;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import java.util.logging.Level;

import static com.ssrn.test.support.utils.RepeaterFluentSyntax.repeat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

public class PageSourceTest {

    private static final Client HTTP_CLIENT = ClientBuilder.newClient().register(JacksonJsonProvider.class);
    private FakeOldPlatform fakeOldPlatform;

    @Before
    public void resetFakeOldPlatform() {
        fakeOldPlatform = new FakeOldPlatform();
        fakeOldPlatform.resetOverrides();
    }

    @Test
    public void shouldRetryRetrievingPageWhenOldPlatformEntitiesFeedReturnedUnexpectedResponse() {
        // Given
        int maxRequestRetry = 3;
        PageSource pageSource = new PageSource(
                HTTP_CLIENT,
                Service.BASE_URL,
                Service.BASIC_AUTH_USERNAME,
                Service.BASIC_AUTH_PASSWORD,
                maxRequestRetry, Level.INFO);

        repeat(() -> fakeOldPlatform.nextResponseWillBe(200, MediaType.TEXT_HTML, "<html></html>"))
                .times(maxRequestRetry - 1);

        // When
        Page entrypointPage = pageSource.getEntrypointPage();

        // Then
        assertThat(entrypointPage.getPapers(), is(not(empty())));
    }

    @Test(expected = RequestRetryLimitedExceeded.class)
    public void shouldThrowExceptionWhenRetryLimitExceededOnOldPlatformEntitiesFeed() {
        // Given
        int maxRequestRetry = 3;
        PageSource pageSource = new PageSource(
                HTTP_CLIENT,
                Service.BASE_URL,
                Service.BASIC_AUTH_USERNAME,
                Service.BASIC_AUTH_PASSWORD,
                maxRequestRetry, Level.INFO);

        repeat(() -> fakeOldPlatform.nextResponseWillBe(200, MediaType.TEXT_HTML, "<html></html>"))
                .times(maxRequestRetry);

        // When
        Page entrypointPage = pageSource.getEntrypointPage();

        // Then
        assertThat(entrypointPage.getPapers(), is(not(empty())));
    }
}
