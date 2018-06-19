package com.ssrn.papers.replicator.http_old_platform_paper_events_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.PaperEvent;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.shared.test_support.event.matchers.AnEventMatcher;
import com.ssrn.papers.shared.test_support.event.matchers.EventWithTypeMatcher;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ssrn.fake_old_platform.Service.PAPER_EVENTS_FEED_EVENTS_PER_PAGE;
import static com.ssrn.papers.shared.test_support.event.matchers.AnEventMatcher.anEventWith;
import static com.ssrn.papers.shared.test_support.event.matchers.EventWithEntityIdMatcher.entityId;
import static com.ssrn.papers.shared.test_support.event.matchers.EventWithEntityTimestampMatcher.entitySameTimestampAndTimezoneAs;
import static com.ssrn.papers.shared.test_support.event.matchers.EventWithEntityVersionMatcher.entityVersion;
import static com.ssrn.papers.shared.test_support.event.matchers.EventWithIdMatcher.id;
import static com.ssrn.papers.shared.test_support.event.matchers.EventWithTypeMatcher.type;
import static com.ssrn.papers.shared.test_support.event.matchers.TitleChangedEventWithTitleMatcher.titleChangedEventTitle;
import static com.ssrn.test.support.http.HttpClient.header;
import static com.ssrn.test.support.http.HttpClient.headers;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.IsCollectionContaining.hasItem;


public class EventsStreamSourceTest extends SsrnFakeOldPlatformTest {

    private FakeOldPlatform fakeOldPlatform;

    @Before
    public void initializeFakeOldPlatform() {
        fakeOldPlatform = new FakeOldPlatform();
    }

    @Test
    public void shouldProvideEventsInOrderTheyAppearInOldPlatformHttpPapersEventFeed() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        List<String> paperTitleChanges = IntStream.range(1, PAPER_EVENTS_FEED_EVENTS_PER_PAGE)
                .mapToObj(eventIndex -> String.format("Paper Title %d %s", eventIndex, UUID.randomUUID()))
                .collect(Collectors.toList());

        PaperSubmissionPage.Visit paperSubmissionPageVisit = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions();

        paperTitleChanges.forEach(paperSubmissionPageVisit::changeTitleTo);
        String abstractId = paperSubmissionPageVisit.getAbstractId();

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);

        // When
        Stream<Paper.Event> streamOfEvents = eventsStreamSource.getEventsStream();

        // Then
        List<Paper.Event> streamedEvents = streamOfEvents.collect(Collectors.toList());

        PaperEvent[] paperEvents = fakeOldPlatform.getAllEventsForPaper(abstractId);
        PaperEvent paperDraftedEvent = Arrays.stream(paperEvents)
                .filter(event -> event.getType().equals("DRAFTED"))
                .findFirst()
                .get();

        assertThat(streamedEvents,
                containsInRelativeOrder(arrayOfMatchers(
                        anEventWith(
                                entityId(abstractId),
                                type(Paper.DraftedEvent.class),
                                entityVersion(1),
                                entitySameTimestampAndTimezoneAs(DateTime.parse(paperDraftedEvent.getTimestamp()))),
                        paperTitleChanges.stream().map(expectedTitle -> anEventWith(
                                entityId(abstractId), type(Paper.TitleChangedEvent.class, titleChangedEventTitle(expectedTitle))
                        ))))
        );
    }

    @Test
    public void shouldSupportEventsThatLackData() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String abstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .makePaperPrivate()
                .getAbstractId();

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);

        // When
        Stream<Paper.Event> streamOfEvents = eventsStreamSource.getEventsStream();

        // Then
        List<Paper.Event> streamedEvents = streamOfEvents.collect(Collectors.toList());
        assertThat(streamedEvents, hasItem(anEventWith(entityId(abstractId), type(Paper.MadePrivateEvent.class))));
    }

    @Test
    public void shouldProvideEventsAfterGivenEventIdInOrderTheyAppearInOldPlatformHttpPapersEventFeed() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);
        List<Paper.Event> streamedEvents = eventsStreamSource.getEventsStream().collect(Collectors.toList());
        String initialNewestEventId = streamedEvents.get(streamedEvents.size() - 1).getId();

        // When

        String thePaperTitle = String.format("Paper Title %s", UUID.randomUUID());

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String theAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo(thePaperTitle)
                .getAbstractId();

        Stream<Paper.Event> streamOfEvents = eventsStreamSource.getEventsStreamStartingAfter(initialNewestEventId);

        // Then
        streamedEvents = streamOfEvents.collect(Collectors.toList());

        assertThat(streamedEvents,
                containsInRelativeOrder(
                        anEventWith(entityId(theAbstractId), type(Paper.DraftedEvent.class)),
                        anEventWith(entityId(theAbstractId), type(Paper.TitleChangedEvent.class, titleChangedEventTitle(thePaperTitle)))
                ));

        assertThat(streamedEvents,
                not(hasItems(
                        anEventWith(id(initialNewestEventId)))
                ));
    }

    @Test
    public void shouldProvideEventsAfterGivenEventIdWhenGivenEventIdIsOnEntrypointPage() {
        // Given
        String initialEntrypointCanonicalUrl = getCurrentPaperEventsFeedWorkingPageCanonicalUrl();

        while (initialEntrypointCanonicalUrl.equals(getCurrentPaperEventsFeedWorkingPageCanonicalUrl())) {
            browser()
                    .visit(ssrnWebsite().loginPage())
                    .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

            browser()
                    .click(ssrnWebsite().navigationBar().submitAPaperLink())
                    .getAbstractId();
        }

        EventsStreamSource eventsStreamSource = new EventsStreamSource("http://localhq.ssrn.com", "username", "password", ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO);
        List<Paper.Event> streamedEvents = eventsStreamSource.getEventsStream().collect(Collectors.toList());
        String initialNewestEventId = streamedEvents.get(streamedEvents.size() - 1).getId();

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String theAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .getAbstractId();

        // When
        Stream<Paper.Event> streamOfEvents = eventsStreamSource.getEventsStreamStartingAfter(initialNewestEventId);

        // Then
        streamedEvents = streamOfEvents.collect(Collectors.toList());

        assertThat(streamedEvents, containsInRelativeOrder(anEventWith(entityId(theAbstractId), type(Paper.DraftedEvent.class))));
        assertThat(streamedEvents, not(hasItems(anEventWith(id(initialNewestEventId)))));
    }

    private String getCurrentPaperEventsFeedWorkingPageCanonicalUrl() {
        Page entrypointPage = httpClient().get(ssrnAbsoluteUrl("/rest/papers/events"), headers(header("Authorization", ssrnBasicAuthenticationHeader()))).readEntity(Page.class);
        return entrypointPage.getLinks().getVia().getHref();
    }

    private static AnEventMatcher[] arrayOfMatchers(AnEventMatcher initialMatcher, Stream<AnEventMatcher> matchers) {
        return Stream.concat(Stream.of(initialMatcher),
                matchers).toArray(AnEventMatcher[]::new);
    }

}
