package com.ssrn.papers.replicator.functional_tests;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Paper;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.http.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.ssrn.test.support.golden_data.SsrnPapers.SECOND_PAPER_IN_SSRN;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReplicatorTest extends SsrnFakeOldPlatformTest {

    private static final String PAPERS_REPLICATOR_BASE_URL = "http://papers-replicator.internal-service";
    private static final String EVENT_REPLICATION_JOB_PATH = "/jobs/event-replication";
    private static final String ENTITY_REPLICATION_JOB_PATH = "/jobs/entity-replication";
    private static final HttpClient HTTP_CLIENT = new HttpClient(ReplicatorTest.class.getName());

    private FakeOldPlatform fakeOldPlatform;
    private PostgresDatabase postgresDatabase;

    @Before
    public void before() {
        postgresDatabase = new PostgresDatabase("localhost", 5432, "postgres", "postgres", "papers");
        fakeOldPlatform = new FakeOldPlatform();
        fakeOldPlatform.resetOverrides();
    }

    @After
    public void cleanupPostgresDatabaseHelper() {
        if (postgresDatabase != null) {
            postgresDatabase.close();
        }
    }

    @Test
    public void shouldReplicateEventsInBackground() {
        // Given
        String thePaperTitle = generateUniquePaperTitle();
        String abstractId = submitAPaperAndChangeTitleTo(thePaperTitle);

        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        assertThat(postgresDatabase.hasPaperWithId(abstractId), is(equalTo(false)));

        assertThat(() -> postgresDatabase.hasPaperWithId(abstractId), eventuallySatisfies(
                is(equalTo(true))
        ).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldNotRunMultipleEventReplicationJobsConcurrently() {
        // Given
        String thePaperTitle = generateUniquePaperTitle();
        String abstractId = submitAPaperAndChangeTitleTo(thePaperTitle);

        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        // When
        List<StatusType> responseStatuses = inParallelRun(() -> HTTP_CLIENT.post(PAPERS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH), 2);

        try {
            // Then
            assertThat(responseStatuses, hasItem(CREATED));
            assertThat(responseStatuses, hasItem(CONFLICT));
        } finally {
            // Guard assertion to ensure the event replication job has completed before we move on to the next test
            assertThat(() -> postgresDatabase.hasPaperWithId(abstractId), eventuallySatisfies(
                    is(equalTo(true))
            ).within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldReplicateEntitiesInBackground() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotPapersFromEndOfEntityFeedInto(postgresDatabase);

        String thePaperTitle = generateUniquePaperTitle();
        Paper paper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(thePaperTitle, null, new String[]{ssrnWebsite().accountId()}, false, false, false, "SUBMITTED");
        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(1, 1));

        // Then
        assertThat(postgresDatabase.hasPaperWithId(Integer.toString(paper.getId())), is(equalTo(false)));

        assertThat(() -> postgresDatabase.hasPaperWithId(Integer.toString(paper.getId())), eventuallySatisfies(
                is(equalTo(true))
        ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldNotRunMultipleEntityReplicationJobsConcurrently() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotPapersFromEndOfEntityFeedInto(postgresDatabase);
        String thePaperTitle = generateUniquePaperTitle();
        Paper paper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(thePaperTitle, null, new String[]{ssrnWebsite().accountId()}, false, false, false, "SUBMITTED");
        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        try {
            // When
            List<StatusType> responseStatuses = inParallelRun(() -> HTTP_CLIENT.post(PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(1, 1)), 2);

            // Then
            assertThat(responseStatuses, hasItem(CREATED));
            assertThat(responseStatuses, hasItem(CONFLICT));

        } finally {
            // Guard assertion to ensure the entity snapshot job is close to finishing before we wait a second for the job to finish
            assertThat(() -> postgresDatabase.hasPaperWithId(Integer.toString(paper.getId())), eventuallySatisfies(
                    is(equalTo(true))
            ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotReplicateMoreThanSpecifiedNumberOfPapersWhenRunningEntityReplicationJob() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotPapersFromEndOfEntityFeedInto(postgresDatabase);
        String[] paperIds = fakeOldPlatformHasHistoricPapers(7);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(5, 1));

        // Then
        assertThat(() -> postgresDatabase.hasPaperWithId(paperIds[4]), eventuallySatisfies(is(equalTo(true)))
                .within(10, SECONDS, checkingEvery(100, MILLISECONDS)));

        sleepFor(1, SECONDS);
        assertThat(() -> postgresDatabase.hasPaperWithId(paperIds[5]), eventuallySatisfies(is(equalTo(false)))
                .within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldCompleteProcessingToTheEndOfEntityFeedAndRollAroundWithinASingleInvocationOfTheEntityReplicationJob() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotPapersFromEndOfEntityFeedInto(postgresDatabase);
        Paper paper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted("Any title", null, new String[0], false, false, false, "SUBMITTED");

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(3, 1));

        // Then
        assertThat(() -> postgresDatabase.hasPaperWithId(Integer.toString(paper.getId())), eventuallySatisfies(is(equalTo(true)))
                .within(10, SECONDS, checkingEvery(100, MILLISECONDS)));

        assertThat(() -> postgresDatabase.getLastEntityFeedCheckpoint(), eventuallySatisfies(is(equalTo(SECOND_PAPER_IN_SSRN.getId())))
                .within(10, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldFilterOutUnescapedNullTerminatorCharactersInPaperEntitiesFeedJsonResponseBodies() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotPapersFromEndOfEntityFeedInto(postgresDatabase);
        String uniqueString = UUID.randomUUID().toString();
        Random random = new Random();
        int entityId = fakeOldPlatform.getNextAbstractId();
        fakeOldPlatform.nextResponseWillBe(200, MediaType.APPLICATION_JSON, String.format("{ \"papers\": [ { \"id\" : %d, \"title\" : \"Paper %s\u0000 Title\", \"version\": %d,  \"submissionStage\":\"SUBMITTED\", \"isRestricted\":false } ] }", entityId, uniqueString, random.nextInt(Integer.MAX_VALUE)));

        // When
        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(1, 1));

        // Then
        assertThat(() -> postgresDatabase.getTitleOfPaperWithId(Integer.toString(entityId)), eventuallySatisfies(
                is(equalTo(String.format("Paper %s Title", uniqueString)))
        ).within(20, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    private List<StatusType> inParallelRun(Callable<Response> responseCallable, int numberOfParallelInvocations) {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfParallelInvocations);
        ExecutorCompletionService<Response> completionService = new ExecutorCompletionService<>(executorService);

        IntStream.range(0, numberOfParallelInvocations).forEach(i -> completionService.submit(responseCallable));

        List<StatusType> responseStatuses = new ArrayList<>();

        while (responseStatuses.size() < numberOfParallelInvocations) {
            try {
                Future<Response> futureResponse = completionService.take();
                responseStatuses.add(futureResponse.get().getStatusInfo());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return responseStatuses;
    }

    private String submitAPaperAndChangeTitleTo(String title) {
        startANewSubmission();

        return ssrnWebsite()
                .paperSubmissionPage()
                .loadedIn(browser(), false)
                .acceptTermsAndConditions()
                .changeTitleTo(title)
                .getAbstractId();
    }

    private void startANewSubmission() {
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink());
    }

    private static String generateUniquePaperTitle() {
        return String.format("Paper Title %s", UUID.randomUUID());
    }

    private static HashMap<String, Integer> entityReplicationJobParameters(final int jobBatchSize, final int databaseUpsertBatchSize) {
        return new HashMap<String, Integer>() {{
            put("jobBatchSize", jobBatchSize);
            put("databaseUpsertBatchSize", databaseUpsertBatchSize);
        }};
    }

    private void ensureNextEntityReplicationJobWillSnaphotPapersFromEndOfEntityFeedInto(PostgresDatabase postgresDatabase) {
        ensureNoReplicationJobsAreRunningAgainst(postgresDatabase);

        String lastEntityFeedCheckpoint = postgresDatabase.getLastEntityFeedCheckpoint();
        String paperId = lastEntityFeedCheckpoint == null ? "0" : lastEntityFeedCheckpoint;

        int remainingNumberOfPapersToReplicationToReachEndOfEntityFeed = fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId(paperId);

        String uniqueString = UUID.randomUUID().toString();
        Paper newPaper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %s Title", uniqueString), null, new String[]{ssrnWebsite().accountId()}, false, false, false, "SUBMITTED");

        HTTP_CLIENT.postAndExpect(CREATED, PAPERS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(remainingNumberOfPapersToReplicationToReachEndOfEntityFeed + 1, 1));

        assertThat(() -> postgresDatabase.hasPaperWithId(Integer.toString(newPaper.getId())), eventuallySatisfies(is(equalTo(true)))
                .within(20, SECONDS, checkingEvery(100, MILLISECONDS)));

        sleepFor(1, SECONDS); // Ugly: delay to allow snapshot job to complete (job only completes once it has retrieved an empty page)
    }

    private static void ensureNoReplicationJobsAreRunningAgainst(PostgresDatabase postgresDatabase) {
        waitUntil(() -> {
            String lastEntityFeedCheckpoint = postgresDatabase.getLastEntityFeedCheckpoint();
            sleepFor(2, SECONDS);
            return lastEntityFeedCheckpoint == null ?
                    postgresDatabase.getLastEntityFeedCheckpoint() == null :
                    postgresDatabase.getLastEntityFeedCheckpoint().equals(lastEntityFeedCheckpoint);
        }).checkingAsFastAsPossible().forNoMoreThan(10, SECONDS);

        sleepFor(1, SECONDS); // Ugly: delay to allow snapshot job to complete (job only completes once it has retrieved an empty page)
    }


    private String[] fakeOldPlatformHasHistoricPapers(int paperCount) {
        return IntStream.range(1, paperCount)
                .mapToObj(i -> {
                    Paper paper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted("Any title", null, new String[0], false, false, false, "SUBMITTED");
                    return Integer.toString(paper.getId());
                })
                .toArray(String[]::new);
    }
}
