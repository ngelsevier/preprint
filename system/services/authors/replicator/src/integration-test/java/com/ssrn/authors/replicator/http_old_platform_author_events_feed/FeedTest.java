package com.ssrn.authors.replicator.http_old_platform_author_events_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Service;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import java.util.logging.Level;

import static com.ssrn.fake_old_platform.Service.AUTHOR_EVENTS_FEED_OLDEST_PAGE_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FeedTest {

    private FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();

    @Before
    public void resetFakeOldPlatform(){
        fakeOldPlatform.resetOverrides();
    }

    @Test
    public void shouldSeekOldestPageInFeed() {
        // Given
        PageSource pageSource =
                new PageSource(ClientBuilder.newClient().register(JacksonJsonProvider.class), "username", "password", 3, Level.INFO);
        Feed feed = new Feed(pageSource, Service.BASE_URL);

        // When
        Page pageReturned = feed.seekOldestPageInFeed();

        // Then
        String expectedOldestPageUrl = UriBuilder.fromPath(Service.BASE_URL)
                .path("/rest/authors/events")
                .path(AUTHOR_EVENTS_FEED_OLDEST_PAGE_ID)
                .toString();

        assertThat(pageReturned.getLinks().getSelf().getHref(), is(equalTo(expectedOldestPageUrl)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowAnExceptionWhenSeekingPageForANonExistantEventId() {
        // Given
        PageSource pageSource = new PageSource(ClientBuilder.newClient().register(JacksonJsonProvider.class), "username", "password", 3, Level.INFO);
        Feed feed = new Feed(pageSource, Service.BASE_URL);

        // When
        feed.seekPageContainingEvent("Non-Existant-OldPlatformEvent-Id");

        // Then
        // exception thrown
    }
}
