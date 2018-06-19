package author_service_contract_test;

import com.ssrn.search.author_updates_subscriber.fake_authors_service.AuthorServiceAuthor;
import com.ssrn.search.author_updates_subscriber.fake_authors_service.AuthorServiceAuthorUpdate;
import com.ssrn.search.author_updates_subscriber.fake_authors_service.FakeAuthorService;
import com.ssrn.test.support.kinesis.KinesisClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Stream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.test.support.golden_data.FakeSsrnUsers.REMOVED_AUTHOR;
import static com.ssrn.test.support.golden_data.RealSsrnUsers.USER_47;
import static com.ssrn.test.support.golden_data.RealSsrnUsers.USER_54;
import static com.ssrn.test.support.kinesis.matchers.AKinesisRecordWithJsonContentMatcher.aKinesisRecordWithContentMatching;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AuthorServiceContract {

    private static final String CONTRACT_TEST_REAL_TARGET = System.getenv("AUTHOR_SERVICE_CONTRACT_TEST_REAL_TARGET");
    private static KinesisClient kinesisClient;

    @BeforeClass
    public static void optionallyConfigureFakeAuthorService() {
        kinesisClient = new KinesisClient("localhost", 4567, "author-updates", 8000, false);

        if (CONTRACT_TEST_REAL_TARGET == null) {
            FakeAuthorService fakeAuthorService = new FakeAuthorService();
            fakeAuthorService.hasEmittedAuthorUpdatesIntoKinesisStream(kinesisClient.getStreamName(),
                    Stream.of(USER_54, USER_47, REMOVED_AUTHOR)
                            .map(author -> new AuthorServiceAuthorUpdate(author.getId(), new AuthorServiceAuthor(author.getId(), author.getPublicDisplayName(), author.isRemoved())))
                            .toArray(AuthorServiceAuthorUpdate[]::new));
        }
    }

    @AfterClass
    public static void cleanUpKinesisStreamHelper() {
        if (kinesisClient != null) {
            kinesisClient.close();
        }
    }

    @Test
    public void expectAuthorServiceToEmitAuthorUpdatesIntoKinesisStream() {
        assertThat(() -> kinesisClient.records(), eventuallySatisfies(
                containsInRelativeOrder(
                        aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(USER_54.getId()))),
                                hasJsonPath("$.author.id", is(equalTo(USER_54.getId()))),
                                hasJsonPath("$.author.name", is(equalTo(USER_54.getPublicDisplayName()))),
                                hasJsonPath("$.author.removed", is(equalTo(USER_54.isRemoved())))
                        ),
                        aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(USER_47.getId()))),
                                hasJsonPath("$.author.id", is(equalTo(USER_47.getId()))),
                                hasJsonPath("$.author.name", is(equalTo(USER_47.getPublicDisplayName()))),
                                hasJsonPath("$.author.removed", is(equalTo(USER_47.isRemoved())))
                        )
                )
        ).within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
    }

}
