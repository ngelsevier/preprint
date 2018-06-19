package com.ssrn.papers.replicator.http_old_platform_paper_entities_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Service;
import com.ssrn.papers.domain.Paper;
import com.ssrn.test.support.golden_data.FakeSsrnUsers;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ssrn.papers.domain.SubmissionStage.APPROVED;
import static com.ssrn.papers.domain.SubmissionStage.SUBMITTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PapersStreamSourceTest {

    private final FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();

    @Test
    public void shouldProvideStreamOfPapersRetrievedFromOldPlatformPaperEntitiesHttpFeed() {
        // Given2
        String uniqueString = UUID.randomUUID().toString();
        String[] authorIds = {FakeSsrnUsers.JOHN_DOE.getId()};

        List<Integer> paperIds = IntStream.range(0, Service.ENTITIES_FEED_ITEMS_PER_PAGE + 1)
                .mapToObj(i -> fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %d Title %s", i + 1, uniqueString), String.format("keywords %s", uniqueString), authorIds, true, true, true, SUBMITTED.name()).getId())
                .collect(Collectors.toList());

        PapersStreamSource paperSource = new PapersStreamSource(
                Service.BASE_URL,
                Service.BASIC_AUTH_USERNAME,
                Service.BASIC_AUTH_PASSWORD,
                ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO
        );

        int numberOfPapersInEntityFeedAfterPaperId = fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId("0");

        // When
        Stream<Paper> papersStream = paperSource.getPapersStream();

        // Then
        List<Paper> retrievedPapers = papersStream
                .limit(numberOfPapersInEntityFeedAfterPaperId)
                .collect(Collectors.toList());

        String[] paperIdsInAscendingOrder = ascendingStreamSortOf(paperIds).map(i -> Integer.toString(i)).toArray(String[]::new);
        assertThat(paperIdsIn(retrievedPapers), containsInRelativeOrder(paperIdsInAscendingOrder));

        Paper firstPaper = retrievedPapers.stream().filter(paper -> paper.getId().equals(paperIdsInAscendingOrder[0])).findFirst().get();
        assertThat(firstPaper.getTitle(), is(equalTo(String.format("Paper 1 Title %s", uniqueString))));
        assertThat(firstPaper.getKeywords(), is(equalTo(String.format("keywords %s", uniqueString))));
        assertThat(firstPaper.getAuthorIds(), is(equalTo(authorIds)));
        assertThat(firstPaper.getVersion(), is(equalTo(1)));
        assertThat(firstPaper.isPaperPrivate(), is(equalTo(true)));
        assertThat(firstPaper.isPaperIrrelevant(), is(equalTo(true)));
        assertThat(firstPaper.isPaperRestricted(), is(equalTo(true)));
        assertThat(firstPaper.getSubmissionStage(), is(equalTo(SUBMITTED)));
    }

    @Test
    public void shouldProvideStreamOfPapersAfterSpecifiedPaperIdRetrievedFromOldPlatformPapersHttpFeed() {
        // Given
        String uniqueString = UUID.randomUUID().toString();
        String[] authorIds = {FakeSsrnUsers.JOHN_DOE.getId()};

        List<Integer> paperIds = IntStream.range(0, Service.ENTITIES_FEED_ITEMS_PER_PAGE + 1)
                .mapToObj(i -> fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(
                        String.format("Paper %d Name %s", i + 1, uniqueString), null, authorIds, i % 2 == 0, i % 3 == 0, i % 2 == 0, "APPROVED").getId())
                .collect(Collectors.toList());

        PapersStreamSource paperSource = new PapersStreamSource(
                Service.BASE_URL,
                Service.BASIC_AUTH_USERNAME,
                Service.BASIC_AUTH_PASSWORD,
                ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO
        );

        int numberOfPapersInEntityFeedAfterPaperId = fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId("0");

        // When
        Stream<Paper> paperStream = paperSource.getPapersStreamAfterId(Integer.toString(paperIds.get(0)));

        // Then
        List<Paper> retrievedPapers = paperStream
                .limit(numberOfPapersInEntityFeedAfterPaperId - 1)
                .collect(Collectors.toList());

        String[] paperIdsInAscendingOrder = ascendingStreamSortOf(paperIds.subList(1, paperIds.size())).map(i -> Integer.toString(i)).toArray(String[]::new);
        List<String> retrievedPaperIds = paperIdsIn(retrievedPapers);
        assertThat(retrievedPaperIds, containsInRelativeOrder(paperIdsInAscendingOrder));
        assertThat(retrievedPaperIds, not(hasItem(Integer.toString(paperIds.get(0)))));

        Paper firstPaper = retrievedPapers.stream().filter(author -> author.getId().equals(paperIdsInAscendingOrder[0])).findFirst().get();
        assertThat(firstPaper.getTitle(), is(equalTo(String.format("Paper 2 Name %s", uniqueString))));
        assertThat(firstPaper.getVersion(), is(greaterThan(0)));
        assertThat(firstPaper.isPaperPrivate(), is(false));
        assertThat(firstPaper.getSubmissionStage(), is(equalTo(APPROVED)));

        Paper secondPaper = retrievedPapers.stream().filter(author -> author.getId().equals(paperIdsInAscendingOrder[1])).findFirst().get();
        assertThat(secondPaper.getTitle(), is(equalTo(String.format("Paper 3 Name %s", uniqueString))));
        assertThat(secondPaper.getVersion(), is(greaterThan(0)));
        assertThat(secondPaper.isPaperPrivate(), is(true));
        assertThat(secondPaper.getSubmissionStage(), is(equalTo(APPROVED)));

    }

    private static List<String> paperIdsIn(List<Paper> retrievedPapers) {
        return retrievedPapers.stream().map(Paper::getId).collect(Collectors.toList());
    }

    private static Stream<Integer> ascendingStreamSortOf(List<Integer> integers) {
        return integers.stream().sorted();
    }

}
