package com.ssrn.authors.replicator.functional_tests;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.http.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReplicatorTest extends SsrnFakeOldPlatformTest {

    private static final String AUTHORS_REPLICATOR_BASE_URL = "http://authors-replicator.internal-service";
    private static final String ENTITY_REPLICATION_JOB_PATH = "/jobs/entity-replication";
    private static final String EVENT_REPLICATION_JOB_PATH = "/jobs/event-replication";
    private static final HttpClient HTTP_CLIENT = new HttpClient(ReplicatorTest.class.getName());

    private FakeOldPlatform fakeOldPlatform;
    private PostgresDatabase postgresDatabase;

    @Before
    public void before() {
        postgresDatabase = new PostgresDatabase("localhost", 5432, "postgres", "postgres", "authors");
        fakeOldPlatform = new FakeOldPlatform();
        fakeOldPlatform.resetOverrides();

        waitUntil(() -> HTTP_CLIENT.get(AUTHORS_REPLICATOR_BASE_URL, "/healthcheck").getStatus() == 200)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(30, SECONDS);
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
        String firstName = String.format("First %s", UUID.randomUUID().toString());
        String lastName = String.format("Last %s", UUID.randomUUID().toString());

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo(firstName, lastName)
                .submitUpdates();

        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, EVENT_REPLICATION_JOB_PATH);

        // Then
        String updatedName = String.format("%s %s", firstName, lastName);
        assertThat(postgresDatabase.getNameOfAuthorWithId(ssrnWebsite().accountId()), is(not(equalTo(updatedName))));

        assertThat(() -> postgresDatabase.getNameOfAuthorWithId(ssrnWebsite().accountId()), eventuallySatisfies(
                is(equalTo(updatedName))
        ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldReplicateEntitiesInBackground() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotAuthorsFromEndOfEntityFeedInto(postgresDatabase);

        String theAuthorName = generateUniqueAuthorName();
        Integer authorId = fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(theAuthorName);
        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(1, 1));

        // Then
        assertThat(postgresDatabase.hasAuthorWithId(Integer.toString(authorId)), is(equalTo(false)));

        assertThat(() -> postgresDatabase.hasAuthorWithId(Integer.toString(authorId)), eventuallySatisfies(
                is(equalTo(true))
        ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldNotRunMultipleEntityReplicationJobsConcurrently() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotAuthorsFromEndOfEntityFeedInto(postgresDatabase);
        String theAuthorName = generateUniqueAuthorName();
        Integer authorId = fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(theAuthorName);
        fakeOldPlatform.delaysNextResponseBy(1, SECONDS);

        try {
            // When
            List<Response.StatusType> responseStatuses = inParallelRun(() -> HTTP_CLIENT.post(AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(1, 1)), 2);

            // Then
            assertThat(responseStatuses, hasItem(CREATED));
            assertThat(responseStatuses, hasItem(CONFLICT));

        } finally {
            // Guard assertion to ensure the entity snapshot job is close to finishing before we wait a second for the job to finish
            assertThat(() -> postgresDatabase.hasAuthorWithId(Integer.toString(authorId)), eventuallySatisfies(
                    is(equalTo(true))
            ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
        }
    }

    @Test
    public void shouldNotReplicateMoreThanSpecifiedNumberOfAuthorsWhenRunningEntityReplicationJob() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotAuthorsFromEndOfEntityFeedInto(postgresDatabase);
        String[] authorIds = fakeOldPlatformHasHistoricAuthors(7);

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(5, 1));

        // Then
        assertThat(() -> postgresDatabase.hasAuthorWithId(authorIds[4]), eventuallySatisfies(is(equalTo(true)))
                .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));

        sleepFor(1, SECONDS);
        assertThat(() -> postgresDatabase.hasAuthorWithId(authorIds[5]), eventuallySatisfies(is(equalTo(false)))
                .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldCompleteProcessingToTheEndOfEntityFeedAndRollAroundWithinASingleInvocationOfTheEntityReplicationJob() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotAuthorsFromEndOfEntityFeedInto(postgresDatabase);
        Integer authorId = fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted("Any name");

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(2, 1));

        // Then
        assertThat(() -> postgresDatabase.hasAuthorWithId(Integer.toString(authorId)), eventuallySatisfies(is(equalTo(true)))
                .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));

        assertThat(() -> postgresDatabase.getLastEntityFeedCheckpoint(), eventuallySatisfies(is(equalTo("1")))
                .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    @Test
    public void shouldFilterOutUnescapedNullTerminatorCharactersInAuthorEntitiesFeedJsonResponseBodies() {
        // Given
        ensureNextEntityReplicationJobWillSnaphotAuthorsFromEndOfEntityFeedInto(postgresDatabase);
        String uniqueString = UUID.randomUUID().toString();
        Random random = new Random();
        int entityId = fakeOldPlatform.getNextAuthorId();
        fakeOldPlatform.nextResponseWillBe(200, MediaType.APPLICATION_JSON, String.format("{ \"authors\": [ { \"id\" : %d, \"name\" : \"Author %s\u0000 Name\", \"version\": %d } ] }", entityId, uniqueString, random.nextInt(Integer.MAX_VALUE)));

        // When
        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(1, 1));

        // Then
        assertThat(() -> postgresDatabase.getNameOfAuthorWithId(Integer.toString(entityId)), eventuallySatisfies(
                is(equalTo(String.format("Author %s Name", uniqueString)))
        ).within(30, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

    private List<Response.StatusType> inParallelRun(Callable<Response> responseCallable, int numberOfParallelInvocations) {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfParallelInvocations);
        ExecutorCompletionService<Response> completionService = new ExecutorCompletionService<>(executorService);

        IntStream.range(0, numberOfParallelInvocations).forEach(i -> completionService.submit(responseCallable));

        List<Response.StatusType> responseStatuses = new ArrayList<>();

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


    private static String generateUniqueAuthorName() {
        return String.format("Author Name %s", UUID.randomUUID());
    }

    private static HashMap<String, Integer> entityReplicationJobParameters(final int jobBatchSize, final int databaseUpsertBatchSize) {
        return new HashMap<String, Integer>() {{
            put("jobBatchSize", jobBatchSize);
            put("databaseUpsertBatchSize", databaseUpsertBatchSize);
        }};
    }

    private void ensureNextEntityReplicationJobWillSnaphotAuthorsFromEndOfEntityFeedInto(PostgresDatabase postgresDatabase) {
        ensureNoReplicationJobsAreRunningAgainst(postgresDatabase);

        String lastEntityFeedCheckpoint = postgresDatabase.getLastEntityFeedCheckpoint();
        String authorId = lastEntityFeedCheckpoint == null ? "0" : lastEntityFeedCheckpoint;

        int remainingNumberOfAuthorsToReplicateToReachEndOfEntityFeed = fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId(authorId);

        String uniqueString = UUID.randomUUID().toString();
        Integer newAuthorId = fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(String.format("Author Name %s", uniqueString));

        HTTP_CLIENT.postAndExpect(CREATED, AUTHORS_REPLICATOR_BASE_URL, ENTITY_REPLICATION_JOB_PATH, entityReplicationJobParameters(remainingNumberOfAuthorsToReplicateToReachEndOfEntityFeed + 1, 1));

        assertThat(() -> postgresDatabase.hasAuthorWithId(Integer.toString(newAuthorId)), eventuallySatisfies(is(equalTo(true)))
                .within(30, SECONDS, checkingEvery(100, MILLISECONDS)));

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


    private String[] fakeOldPlatformHasHistoricAuthors(int authorCount) {
        return IntStream.range(1, authorCount)
                .mapToObj(i -> {
                    Integer authorId = fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted("Any name");
                    return Integer.toString(authorId);
                })
                .toArray(String[]::new);
    }
}
