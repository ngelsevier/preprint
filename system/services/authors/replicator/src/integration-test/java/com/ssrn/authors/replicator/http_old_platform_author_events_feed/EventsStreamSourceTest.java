package com.ssrn.authors.replicator.http_old_platform_author_events_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.authors.domain.Event;
import com.ssrn.authors.shared.test_support.matchers.AnEventMatcher;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.ssrn.website.pagemodel.PersonalInformationPage;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.authors.shared.test_support.matchers.AnEventMatcher.anEventWith;
import static com.ssrn.authors.shared.test_support.matchers.EventOfTypeMatcher.type;
import static com.ssrn.authors.shared.test_support.matchers.EventWithEntityIdMatcher.entityId;
import static com.ssrn.authors.shared.test_support.matchers.EventWithIdMatcher.id;
import static com.ssrn.authors.shared.test_support.matchers.EventWithJsonDataMatcher.dataSatisfying;
import static com.ssrn.fake_old_platform.Service.AUTHOR_EVENTS_FEED_EVENTS_PER_PAGE;
import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;


public class EventsStreamSourceTest extends SsrnFakeOldPlatformTest {

    @Test
    public void shouldProvideEventsInOrderTheyAppearInOldPlatformHttpAuthorsEventFeed() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        final String lastname = "Lastname";
        List<String> authorfirstnamechanges = IntStream.range(1, AUTHOR_EVENTS_FEED_EVENTS_PER_PAGE)
                .mapToObj(eventIndex -> String.format("First Name %d %d", eventIndex, new Random().nextInt(9999)))
                .collect(Collectors.toList());

        PersonalInformationPage.Visit personalInformationPageVisit = browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink());

        authorfirstnamechanges.forEach(firstname -> personalInformationPageVisit.enterPublicDisplayNameTo(firstname, lastname).submitUpdates());

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);

        // When
        Stream<Event> streamOfEvents = eventsStreamSource.getEventsStream();

        // Then
        List<Event> streamedEvents = streamOfEvents.collect(Collectors.toList());

        assertThat(streamedEvents,
                containsInRelativeOrder(arrayOfMatchers(
                        authorfirstnamechanges.stream().map(firstname -> anEventWith(
                                entityId(ssrnWebsite().accountId()), type("NAME CHANGED"),
                                dataSatisfying(hasJsonPath("$.name", is(equalTo(String.format("%s %s", firstname, lastname))))))
                        )))
        );
    }

    @Test
    public void shouldProvideEventsAfterGivenEventIdInOrderTheyAppearInOldPlatformHttpAuthorEventsFeed() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        String theFirstName = String.format("Firstname %d", new Random().nextInt(9999));

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);
        List<Event> streamedEvents = eventsStreamSource.getEventsStream().collect(Collectors.toList());
        String initialNewestEventId = streamedEvents.get(streamedEvents.size() - 1).getId();

        // When

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        PersonalInformationPage.Visit personalInfoPageVisit = browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink());

        personalInfoPageVisit.enterPublicDisplayNameTo(theFirstName, "Lastname").submitUpdates();

        Stream<Event> streamOfEvents = eventsStreamSource.getEventsStreamStartingAfter(initialNewestEventId);

        // Then
        streamedEvents = streamOfEvents.collect(Collectors.toList());

        assertThat(streamedEvents,
                containsInRelativeOrder(anEventWith(
                        entityId(ssrnWebsite().accountId()), type("NAME CHANGED"),
                        dataSatisfying(hasJsonPath("$.name", is(equalTo(String.format("%s %s", theFirstName, "Lastname"))))))
                )
        );

        assertThat(streamedEvents,
                not(hasItems(
                        anEventWith(id(initialNewestEventId)))
                ));
    }

    @Test
    public void shouldProvideEventsAfterGivenEventIdWhenGivenEventIdIsOnEntrypointPage() {
        // Given
        String initialEntrypointCanonicalUrl = getCurrentAuthorEventsFeedWorkingPageCanonicalUrl();

        while (initialEntrypointCanonicalUrl.equals(getCurrentAuthorEventsFeedWorkingPageCanonicalUrl())) {
            browser()
                    .visit(ssrnWebsite().loginPage())
                    .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

            browser()
                    .click(ssrnWebsite().navigationBar().submitAPaperLink());

            browser()
                    .visit(ssrnWebsite().loginPage())
                    .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

            PersonalInformationPage.Visit personalInfoPageVisit = browser()
                    .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink());

            personalInfoPageVisit.enterPublicDisplayNameTo(String.format("Firstname %d", new Random().nextInt(9999)), "Lastname").submitUpdates();
        }

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);
        List<Event> streamedEvents = eventsStreamSource.getEventsStream().collect(Collectors.toList());
        String initialNewestEventId = streamedEvents.get(streamedEvents.size() - 1).getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        PersonalInformationPage.Visit personalInfoPageVisit = browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink());

        personalInfoPageVisit.enterPublicDisplayNameTo(String.format("Firstname %d", new Random().nextInt(9999)), "Lastname").submitUpdates();

        // When
        Stream<Event> streamOfEvents = eventsStreamSource.getEventsStreamStartingAfter(initialNewestEventId);

        // Then
        streamedEvents = streamOfEvents.collect(Collectors.toList());

        assertThat(streamedEvents, containsInRelativeOrder(anEventWith(entityId(ssrnWebsite().accountId()), type("NAME CHANGED"))));
        assertThat(streamedEvents, not(hasItems(anEventWith(id(initialNewestEventId)))));
    }

    private String getCurrentAuthorEventsFeedWorkingPageCanonicalUrl() {
        Page entrypointPage = httpClient().get(ssrnAbsoluteUrl("/rest/authors/events"), headers(header("Authorization", ssrnBasicAuthenticationHeader()))).readEntity(Page.class);
        return entrypointPage.getLinks().getVia().getHref();
    }

    private static AnEventMatcher[] arrayOfMatchers(Stream<AnEventMatcher> matchers) {
        return matchers.toArray(AnEventMatcher[]::new);
    }

}
