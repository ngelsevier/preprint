package com.ssrn.authors.replicator.http_old_platform_author_entities_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.authors.replicator.RequestRetryLimitedExceeded;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Service;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public void shouldRetryRetrievingPageWhenOldPlatformAuthorsEntitiesFeedReturnedUnexpectedResponse() {
        // Given
        IntStream.range(0, Service.ENTITIES_FEED_ITEMS_PER_PAGE + 1)
                .mapToObj(i -> fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(String.format("Author %d Name", i + 1)))
                .collect(Collectors.toList());

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
        assertThat(entrypointPage.getAuthors(), is(not(empty())));
    }

    @Test(expected = RequestRetryLimitedExceeded.class)
    public void shouldThrowExceptionWhenRetryLimitExceededOnOldPlatformAuthorsEntitiesFeed() {
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
        assertThat(entrypointPage.getAuthors(), is(not(empty())));
    }
}
