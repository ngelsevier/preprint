package com.ssrn.papers.service_test;

import com.amazonaws.services.kinesis.model.Record;
import com.jayway.jsonpath.JsonPath;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.kinesis.KinesisClient;
import com.ssrn.test.support.kinesis.matchers.AKinesisRecordWithJsonContentMatcher;
import com.ssrn.test.support.logging.OverrideLogbackRootLoggerLevel;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.*;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.qos.logback.classic.Level.ERROR;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.fake_old_platform.Service.PAPER_EVENTS_FEED_EVENTS_PER_PAGE;
import static com.ssrn.test.support.kinesis.matchers.AKinesisRecordWithJsonContentMatcher.aKinesisRecordWithContentMatching;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class PaperServiceTest extends SsrnFakeOldPlatformTest {

    private static final int NUMBER_OF_PAGES = 2;
    private static final String PAPERS_REPLICATOR_BASE_URL = "http://papers-replicator.internal-service";
    private static final String EVENT_REPLICATION_JOB_PATH = "/jobs/event-replication";
    private static final String ENTITY_REPLICATION_JOB_PATH = "/jobs/entity-replication";
    private static final HttpClient HTTP_CLIENT = new HttpClient(PaperServiceTest.class.getName());

    private static KinesisClient kinesisClient;
    private FakeOldPlatform fakeOldPlatform;

    @Rule
    public OverrideLogbackRootLoggerLevel overrideLogbackRootLoggerLevel = new OverrideLogbackRootLoggerLevel(ERROR);

    @BeforeClass
    public static void createKinesisStreamHelper() {
        kinesisClient = new KinesisClient("localhost", 4567, "papers", 8000, true);

        waitUntil(() -> HTTP_CLIENT.get(PAPERS_REPLICATOR_BASE_URL, "/healthcheck").getStatusInfo().equals(Response.Status.OK))
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(15, SECONDS);
    }

    @AfterClass
    public static void cleanUpKinesisStreamHelper() throws Exception {
        kinesisClient.close();
    }

    @Before
    public void resetKinesisStreamHelper() {
        kinesisClient.forgetReceivedRecords();
        fakeOldPlatform = new FakeOldPlatform();
        ensureNextPaperReplicationJobWillBeginAtTheEndOfTheEntityFeed(fakeOldPlatform);
    }

    @Test
    public void shouldPublishPaperUpdatesIntoKinesisPaperStreamInResponseToOldPlatformPaperEvents() {
        // Given, When
        String abstractId = startANewSubmission().getAbstractId();

        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(abstractId)),
                        hasJsonPath("$.title", equalTo(abstractId))
                        )
                )
        ).within(20, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        // When
        kinesisClient.forgetReceivedRecords();
        PaperSubmissionPage.Visit paperSubmissionPageVisit = ssrnWebsite().paperSubmissionPage().loadedIn(browser(), false)
                .acceptTermsAndConditions();

        String uniqueString = UUID.randomUUID().toString();
        List<String> paperTitleChanges = IntStream.range(1, PAPER_EVENTS_FEED_EVENTS_PER_PAGE * NUMBER_OF_PAGES)
                .mapToObj(eventIndex -> String.format("The Paper's Фінансово Title is %d %s", eventIndex, uniqueString))
                .collect(Collectors.toList());

        paperTitleChanges.forEach(paperSubmissionPageVisit::changeTitleTo);

        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                containsInRelativeOrder(
                        paperTitleChanges.stream().map(title ->
                                aKinesisRecordWithContentMatching(
                                        hasJsonPath("$.id", equalTo(abstractId)),
                                        hasJsonPath("$.title", equalTo(title))
                                )
                        ).toArray(AKinesisRecordWithJsonContentMatcher[]::new))
        ).within(10, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        assertThat(kinesisClient.records(), not(hasItem(
                aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", equalTo(abstractId)),
                        hasJsonPath("$.title", equalTo(abstractId))
                )
        )));

        paperSubmissionPageVisit.addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId());

        kinesisClient.forgetReceivedRecords();
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(kinesisClient::records, eventuallySatisfies(
                hasItem(
                        aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", equalTo(abstractId)),
                                hasJsonPath("$.title", equalTo(paperTitleChanges.get(paperTitleChanges.size() - 1))),
                                hasJsonPath("$.authorIds", containsInRelativeOrder(ssrnWebsite().accountId(), ssrnWebsite().firstAdditionalAuthorAccountId()))
                        )
                )
        ).within(10, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));


    }

    @Test
    public void shouldPublishPapersFromOldPlatformPapersFeedIntoKinesisPapersStream() {
        // Given
        String firstPaperTitle = String.format("Some new paper title %s", UUID.randomUUID().toString());

        String firstAbstractId = startANewSubmission()
                .acceptTermsAndConditions()
                .changeTitleTo(firstPaperTitle)
                .addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId())
                .getAbstractId();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", 1);
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(
                kinesisClient::records,
                eventuallySatisfies(
                        hasItem(aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(firstAbstractId))),
                                hasJsonPath("$.title", is(equalTo(firstPaperTitle))),
                                hasJsonPath("$.authorIds", containsInRelativeOrder(ssrnWebsite().accountId(), ssrnWebsite().firstAdditionalAuthorAccountId()))
                                )
                        )
                ).within(15, SECONDS, checkingEvery(100, MILLISECONDS)));

        // Given
        kinesisClient.forgetReceivedRecords();
        String secondPaperTitle = String.format("Some new paper title %s", UUID.randomUUID().toString());

        PaperSubmissionPage.Visit secondPaperSubmissionPageVisit = startANewSubmission();
        String secondAbstractId = secondPaperSubmissionPageVisit.getAbstractId();
        secondPaperSubmissionPageVisit.acceptTermsAndConditions().changeTitleTo(secondPaperTitle);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", 1);
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(
                kinesisClient::records,
                eventuallySatisfies(allOf(
                        not(hasItem(aKinesisRecordWithContentMatching(hasJsonPath("$.id", is(equalTo(firstAbstractId)))))),
                        hasItem(aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(secondAbstractId))),
                                hasJsonPath("$.title", is(equalTo(secondPaperTitle))),
                                hasJsonPath("$.authorIds", contains(ssrnWebsite().accountId()))
                        )))).within(15, SECONDS, checkingEvery(100, MILLISECONDS))
        );

        // Given
        kinesisClient.forgetReceivedRecords();
        int corpusSize = fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId("0");

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", corpusSize);
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(
                kinesisClient::records,
                eventuallySatisfies(hasItems(
                        aKinesisRecordWithContentMatching(hasJsonPath("$.id", is(equalTo(firstAbstractId)))),
                        aKinesisRecordWithContentMatching(hasJsonPath("$.id", is(equalTo(secondAbstractId))))
                )).within(15, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldIgnoreOldPlatformPaperEventsThatDoNotIncreasePaperVersion() {
        // Given
        String paperTitle = String.format("A paper title %s", UUID.randomUUID().toString());
        String abstractId = startANewSubmission()
                .acceptTermsAndConditions()
                .changeTitleTo(paperTitle)
                .getAbstractId();

        // When
        sleepFor(5, SECONDS);
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", 1);
            put("databaseUpsertBatchSize", 1);
        }});

        // Then
        assertThat(() -> kinesisClient.records(), eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", is(equalTo(abstractId))),
                        hasJsonPath("$.title", is(equalTo(paperTitle)))
                ))
        ).within(10, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        // Given
        kinesisClient.forgetReceivedRecords();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);
        sleepFor(3, SECONDS);

        // Then
        assertThat(kinesisClient.records(), not(hasItem(
                aKinesisRecordWithContentMatching(hasJsonPath("$.id", is(equalTo(abstractId))))
        )));
    }

    @Test
    public void shouldOverwriteOldPlatformPaperEventsWithOldPlatformPaper() {
        // Given
        String paperTitle = String.format("A paper title %s", UUID.randomUUID().toString());
        String abstractId = startANewSubmission()
                .acceptTermsAndConditions()
                .changeTitleTo(paperTitle)
                .getAbstractId();

        // When
        sleepFor(5, SECONDS);
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(() -> kinesisClient.records(), eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", is(equalTo(abstractId))),
                        hasJsonPath("$.title", is(equalTo(paperTitle)))
                ))
        ).within(10, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));

        // Given
        kinesisClient.forgetReceivedRecords();

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", 1);
            put("databaseUpsertBatchSize", 1);
        }});
        sleepFor(3, SECONDS);

        // Then
        assertThat(() -> kinesisClient.records(), eventuallySatisfies(
                hasItem(aKinesisRecordWithContentMatching(
                        hasJsonPath("$.id", is(equalTo(abstractId)))
                ))
        ).within(10, TimeUnit.SECONDS, checkingEvery(100, TimeUnit.MILLISECONDS)));
    }

    private PaperSubmissionPage.Visit startANewSubmission() {
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        return browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());
    }

    private static void ensureNextPaperReplicationJobWillBeginAtTheEndOfTheEntityFeed(FakeOldPlatform fakeOldPlatform) {
        ensureNoReplicatorJobsAreRunning();

        kinesisClient.forgetReceivedRecords();

        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", 1);
            put("databaseUpsertBatchSize", 1);
        }});

        waitUntil(() -> kinesisClient.records().size() == 1)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(20, SECONDS);

        Record record = kinesisClient.records().get(0);
        String idOfLastPaperProcessedByReplicationJob = getRecordContentValueAtJsonPath("$.id", record, String.class);
        int remainingNumberOfPapersToReplicateToReachEndOfEntityFeed = fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId(idOfLastPaperProcessedByReplicationJob);

        kinesisClient.forgetReceivedRecords();
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, new HashMap<String, Integer>() {{
            put("jobBatchSize", remainingNumberOfPapersToReplicateToReachEndOfEntityFeed);
            put("databaseUpsertBatchSize", 1);
        }});

        waitUntil(() -> kinesisClient.records().size() == remainingNumberOfPapersToReplicateToReachEndOfEntityFeed)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(20, SECONDS);

        sleepFor(1, SECONDS);  // Ugly: delay to allow snapshot job to complete (job only completes once it has retrieved an empty page)
    }

    private static void ensureNoReplicatorJobsAreRunning() {
        waitUntil(() -> {
            String lastSequenceNumber = kinesisClient.records().stream().reduce((x, y) -> y).map(Record::getSequenceNumber).orElse(null);
            sleepFor(2, SECONDS);
            String lastSequenceNumber2SecondsLater = kinesisClient.records().stream().reduce((x, y) -> y).map(Record::getSequenceNumber).orElse(null);

            return Objects.equals(lastSequenceNumber, lastSequenceNumber2SecondsLater);
        }).checkingAsFastAsPossible().forNoMoreThan(20, SECONDS);

        sleepFor(1, SECONDS);  // Ugly: delay to allow snapshot job to complete (job only completes once it has retrieved an empty page)
    }

    private static <T> T getRecordContentValueAtJsonPath(String jsonPath, Record record, Class<T> valueType) {
        return JsonPath.parse(new String(record.getData().array())).read(jsonPath, valueType);
    }
}
