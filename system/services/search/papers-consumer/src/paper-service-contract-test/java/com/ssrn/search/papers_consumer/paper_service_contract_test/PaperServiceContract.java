package com.ssrn.search.papers_consumer.paper_service_contract_test;

import com.ssrn.search.papers_consumer.fake_papers_service.FakePaperService;
import com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper;
import com.ssrn.test.support.golden_data.SsrnUser;
import com.ssrn.test.support.kinesis.KinesisClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.search.papers_consumer.fake_papers_service.PaperServicePaper.SubmissionStage.fromString;
import static com.ssrn.test.support.golden_data.SsrnPapers.PAPER_48;
import static com.ssrn.test.support.golden_data.SsrnPapers.PAPER_535;
import static com.ssrn.test.support.golden_data.SsrnPapers.PAPER_87;
import static com.ssrn.test.support.kinesis.matchers.AKinesisRecordWithJsonContentMatcher.aKinesisRecordWithContentMatching;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class PaperServiceContract {

    private static KinesisClient kinesisClient;

    @BeforeClass
    public static void optionallyConfigureFakePaperService() {
        if (System.getenv("PAPER_SERVICE_CONTRACT_TEST_REAL_TARGET") == null) {
            FakePaperService fakePaperService = new FakePaperService();
            fakePaperService.hasEmittedPapersIntoKinesisStream("papers",
                    Stream.of(PAPER_48, PAPER_87, PAPER_535)
                            .map(ssrnPaper -> new PaperServicePaper(ssrnPaper.getId(), ssrnPaper.getTitle(), ssrnPaper.getKeywords(), Arrays.stream(ssrnPaper.getAuthors()).map(SsrnUser::getId).toArray(String[]::new), ssrnPaper.isPrivate(), ssrnPaper.isIrrelevant(), ssrnPaper.isRestricted(), fromString(ssrnPaper.getSubmissionStage())))
                            .toArray(PaperServicePaper[]::new)
            );
        }
    }

    @BeforeClass
    public static void createKinesisStreamHelper() {
        kinesisClient = new KinesisClient("localhost", 4567, "papers", 8000, false);
    }

    @AfterClass
    public static void cleanUpKinesisStreamHelper() {
        if (kinesisClient != null) {
            kinesisClient.close();
        }
    }

    @Test
    public void expectPaperServiceToEmitPapersIntoKinesisStream() {
        assertThat(() -> kinesisClient.records(), eventuallySatisfies(
                containsInRelativeOrder(
                        aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(PAPER_48.getId()))),
                                hasJsonPath("$.title", is(equalTo(PAPER_48.getTitle()))),
                                hasJsonPath("$.paperPrivate", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.paperIrrelevant", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.paperRestricted", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.submissionStage", is(equalTo(PAPER_48.getSubmissionStage()))),
                                not(hasJsonPath("$.keywords"))
                        ),
                        aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(PAPER_87.getId()))),
                                hasJsonPath("$.title", is(equalTo(PAPER_87.getTitle()))),
                                hasJsonPath("$.paperPrivate", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.paperIrrelevant", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.paperRestricted", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.submissionStage", is(equalTo(PAPER_87.getSubmissionStage()))),
                                not(hasJsonPath("$.keywords"))
                        ),
                        aKinesisRecordWithContentMatching(
                                hasJsonPath("$.id", is(equalTo(PAPER_535.getId()))),
                                hasJsonPath("$.title", is(equalTo(PAPER_535.getTitle()))),
                                hasJsonPath("$.paperPrivate", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.paperIrrelevant", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.paperRestricted", is(equalTo(Boolean.FALSE))),
                                hasJsonPath("$.submissionStage", is(equalTo(PAPER_535.getSubmissionStage()))),
                                hasJsonPath("$.keywords", is(equalTo(PAPER_535.getKeywords()))))
                )).within(60, SECONDS, checkingEvery(100, MILLISECONDS)));
    }


}
