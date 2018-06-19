package com.ssrn.authors.service_test;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.kinesis.KinesisClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.kinesis.matchers.AKinesisRecordWithJsonContentMatcher.aKinesisRecordWithContentMatching;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class AuthorServiceTest extends SsrnFakeOldPlatformTest {

    private static final String AUTHORS_REPLICATOR_BASE_URL = "http://authors-replicator.internal-service";
    private static final String ENTITY_REPLICATION_JOB_PATH = "/jobs/entity-replication";
    private static final String EVENT_REPLICATION_JOB_PATH = "/jobs/event-replication";
    private static final HttpClient HTTP_CLIENT = new HttpClient(AuthorServiceTest.class.getName());
    private static KinesisClient kinesisClient;
    private FakeOldPlatform fakeOldPlatform;

    @BeforeClass
    public static void createKinesisStreamHelper() {
        kinesisClient = new KinesisClient("localhost", 4567, "author-updates", 8000, true);

        waitUntil(() -> HTTP_CLIENT.get(AUTHORS_REPLICATOR_BASE_URL, "/healthcheck").getStatusInfo().equals(Response.Status.OK))
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(15, SECONDS);
    }

    @AfterClass
    public static void cleanUpKinesisStreamHelper() {
        kinesisClient.close();
    }

    @Before
    public void resetKinesisStreamHelper() {
        kinesisClient.forgetReceivedRecords();
        fakeOldPlatform = new FakeOldPlatform();
    }

    @Test
    public void shouldPublishAnAuthorUpdateIntoKinesisAuthorUpdatesStreamForEachAuthorInOldPlatformAuthorsFeed() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo("Jon", "Pertwee")
                .submitUpdates();

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId("0"));
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.name", equalTo("Jon Pertwee"))
                        )
                )
        ).within(20, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        // When
        browser().visit(ssrnWebsite().userHomePage());
        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo("Tom", "Baker")
                .submitUpdates();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId("0"));
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.name", equalTo("Tom Baker"))
                        )
                )
        ).within(20, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));
    }

    @Test
    public void shouldPublishAuthorUpdatesIntoKinesisAuthorUpdateStreamInResponseToOldPlatformAuthorEvents() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo("Harvey", "Nickels")
                .submitUpdates();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.name", equalTo("Harvey Nickels"))
                        )
                )
        ).within(20, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));
    }

    @Test
    public void shouldIgnoreOldPlatformAuthorEventsThatDoNotIncreaseAuthorVersion() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo("Jon", "Pertwee")
                .submitUpdates();

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId("0"));
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.name", equalTo("Jon Pertwee"))
                        )
                )
        ).within(20, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        // Given
        kinesisClient.forgetReceivedRecords();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);
        sleepFor(3, SECONDS);

        // Then
        assertThat(kinesisClient.records(), not(hasItem(
                aKinesisRecordWithContentMatching(hasJsonPath("$.id", is(equalTo(ssrnWebsite().accountId()))))
        )));
    }

    @Test
    public void shouldOverwriteOldPlatformAuthorEventsWithOldPlatformAuthor() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo("Peter", "Davidson")
                .submitUpdates();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.id", equalTo(ssrnWebsite().accountId())),
                        hasJsonPath("$.author.name", equalTo("Peter Davidson"))
                        )
                )
        ).within(20, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        // Given
        kinesisClient.forgetReceivedRecords();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId("0"));
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(() -> kinesisClient.records(), eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", is(equalTo(ssrnWebsite().accountId())))
                ))
        ).within(10, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));
    }

}
