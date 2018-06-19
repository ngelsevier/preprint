package com.ssrn.papers.replicator;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.domain.PaperRepository;
import com.ssrn.papers.domain.SubmissionStage;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ssrn.papers.domain.SubmissionStage.*;
import static com.ssrn.papers.shared.test_support.event.PaperBuilder.aPaper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaperReplicatorTest {

    @SuppressWarnings("unchecked")
    private static final FeedJobCheckpointer<Paper> ANY_PAPER_CHECKPOINTER = (FeedJobCheckpointer<Paper>) mock(FeedJobCheckpointer.class);

    @Test
    public void shouldSavePaperToPaperRepositoryForEachPaperInStream() {

        // Given
        OldPlatformPapersStreamSource oldPlatformPapersStreamSource = mock(OldPlatformPapersStreamSource.class);

        PaperRepository paperRepository = mock(PaperRepository.class);

        PaperReplicator paperReplicator = new PaperReplicator(oldPlatformPapersStreamSource, paperRepository, ANY_PAPER_CHECKPOINTER);

        Paper[] papers = {
                aPaper().withId("101").withPaperPrivate(false).build(),
                aPaper().withId("102").withPaperPrivate(true).build(),
                aPaper().withId("103").build(),
                aPaper().withId("104").withPaperIrrelevant(true).build(),
                aPaper().withId("105").withPaperIrrelevant(false).build(),
                aPaper().withId("106").withPaperRestricted(true).build(),
                aPaper().withId("107").withPaperRestricted(false).build(),
                aPaper().withId("108").withSubmissionStage(SUBMITTED).build(),
                aPaper().withId("109").withKeywords("any string").build()
        };

        when(oldPlatformPapersStreamSource.getPapersStream())
                .thenReturn(Stream.of(papers));

        // When
        paperReplicator.replicatePapers(1000, 100);

        // Then
        ArgumentCaptor<List<Paper>> paperArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        List<Paper> savedPapers = paperArgumentCaptor.getValue();
        assertThat(savedPapers, hasSize(9));
        assertThat(savedPapers.get(0).getId(), is(equalTo("101")));
        assertThat(savedPapers.get(0).isPaperPrivate(), is(equalTo(false)));
        assertThat(savedPapers.get(1).getId(), is(equalTo("102")));
        assertThat(savedPapers.get(1).isPaperPrivate(), is(equalTo(true)));
        assertThat(savedPapers.get(2).getId(), is(equalTo("103")));
        assertThat(savedPapers.get(2).isPaperPrivate(), is(equalTo(false)));
        assertThat(savedPapers.get(2).isPaperIrrelevant(), is(equalTo(false)));
        assertThat(savedPapers.get(3).getId(), is(equalTo("104")));
        assertThat(savedPapers.get(3).isPaperIrrelevant(), is(equalTo(true)));
        assertThat(savedPapers.get(4).getId(), is(equalTo("105")));
        assertThat(savedPapers.get(4).isPaperIrrelevant(), is(equalTo(false)));
        assertThat(savedPapers.get(5).getId(), is(equalTo("106")));
        assertThat(savedPapers.get(5).isPaperRestricted(), is(equalTo(true)));
        assertThat(savedPapers.get(6).getId(), is(equalTo("107")));
        assertThat(savedPapers.get(6).isPaperRestricted(), is(equalTo(false)));
        assertThat(savedPapers.get(7).getId(), is(equalTo("108")));
        assertThat(savedPapers.get(7).getSubmissionStage(), is(equalTo(SUBMITTED)));
        assertThat(savedPapers.get(8).getId(), is(equalTo("109")));
        assertThat(savedPapers.get(8).getKeywords(), is(equalTo("any string")));
    }

    @Test
    public void shouldNotSaveMoreThanSpecifiedNumberOfPapers() {
        // Given
        OldPlatformPapersStreamSource oldPlatformPapersStreamSource = mock(OldPlatformPapersStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        PaperReplicator paperReplicator = new PaperReplicator(oldPlatformPapersStreamSource, paperRepository, ANY_PAPER_CHECKPOINTER);

        when(oldPlatformPapersStreamSource.getPapersStream())
                .thenReturn(Stream.of(aPaper().withId("101").build(), aPaper().withId("102").build(), aPaper().withId("103").build()));

        // When
        paperReplicator.replicatePapers(2, 100);

        // Then
        ArgumentCaptor<List<Paper>> paperArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paperRepository, times(1)).save(paperArgumentCaptor.capture());
        List<Paper> savedPapers = paperArgumentCaptor.getValue();
        assertThat(savedPapers, hasSize(2));
        assertThat(savedPapers.get(0).getId(), is(equalTo("101")));
        assertThat(savedPapers.get(1).getId(), is(equalTo("102")));
    }

    @Test
    public void shouldNotSaveMoreThanSpecifiedNumberOfPapersPerRepositoryRequest() {
        // Given
        OldPlatformPapersStreamSource oldPlatformPapersStreamSource = mock(OldPlatformPapersStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        PaperReplicator paperReplicator = new PaperReplicator(oldPlatformPapersStreamSource, paperRepository, ANY_PAPER_CHECKPOINTER);

        when(oldPlatformPapersStreamSource.getPapersStream())
                .thenReturn(Stream.of(aPaper().withId("101").build(), aPaper().withId("102").build(), aPaper().withId("103").build()));

        // When
        paperReplicator.replicatePapers(1000, 2);

        // Then
        ArgumentCaptor<List<Paper>> paperArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paperRepository, times(2)).save(paperArgumentCaptor.capture());
        List<Paper> firstBatchOfSavedPaper = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstBatchOfSavedPaper, hasSize(2));
        assertThat(firstBatchOfSavedPaper.get(0).getId(), is(equalTo("101")));
        assertThat(firstBatchOfSavedPaper.get(1).getId(), is(equalTo("102")));

        List<Paper> secondBatchOfSavedPaper = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondBatchOfSavedPaper, hasSize(1));
        assertThat(secondBatchOfSavedPaper.get(0).getId(), is(equalTo("103")));
    }

    @Test
    public void shouldResumeProcessingPapersFromWhereLastJobLeftOff() {
        // Given
        OldPlatformPapersStreamSource oldPlatformPapersStreamSource = mock(OldPlatformPapersStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        InMemoryEntityFeedJobCheckpointer paperCheckpointer = new InMemoryEntityFeedJobCheckpointer();

        PaperReplicator paperReplicator = new PaperReplicator(oldPlatformPapersStreamSource, paperRepository, paperCheckpointer);

        when(oldPlatformPapersStreamSource.getPapersStream())
                .thenReturn(Stream.of(aPaper().withId("100").build(), aPaper().withId("101").build()));

        Paper[] papers = {aPaper().withId("102").build(), aPaper().withId("103").build()};
        when(oldPlatformPapersStreamSource.getPapersStreamAfterId("101"))
                .thenReturn(Stream.of(papers));

        paperReplicator.replicatePapers(1000, 100);

        // When
        paperReplicator.replicatePapers(1000, 100);

        // Then
        ArgumentCaptor<List<Paper>> paperArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paperRepository, times(2)).save(paperArgumentCaptor.capture());

        List<Paper> firstBatchOfSavedPapers = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstBatchOfSavedPapers, hasSize(2));
        assertThat(firstBatchOfSavedPapers.get(0).getId(), is(equalTo("100")));
        assertThat(firstBatchOfSavedPapers.get(1).getId(), is(equalTo("101")));

        List<Paper> secondBatchOfSavedPapers = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondBatchOfSavedPapers, hasSize(2));
        assertThat(secondBatchOfSavedPapers.get(0).getId(), is(equalTo("102")));
        assertThat(secondBatchOfSavedPapers.get(1).getId(), is(equalTo("103")));
    }

    @Test
    public void shouldResumeProcessingPapersAfterMostRecentlyProcessedPaperInStream() {
        // Given
        OldPlatformPapersStreamSource oldPlatformPapersStreamSource = mock(OldPlatformPapersStreamSource.class);
        PaperRepository paperRepository = mock(PaperRepository.class);

        InMemoryEntityFeedJobCheckpointer paperCheckpointer = new InMemoryEntityFeedJobCheckpointer();

        PaperReplicator paperReplicator = new PaperReplicator(oldPlatformPapersStreamSource, paperRepository, paperCheckpointer);

        Paper paper101 = aPaper().withId("101").build();
        Paper paper102 = aPaper().withId("102").build();

        when(oldPlatformPapersStreamSource.getPapersStream())
                .thenReturn(Stream.of(paper101))
                .thenReturn(Stream.of(paper101, paper102));

        doThrow(new RuntimeException("Could not save paper"))
                .doNothing()
                .when(paperRepository).save(any(List.class));

        try {
            paperReplicator.replicatePapers(1000, 100);
        } catch (RuntimeException ignored) {
        }

        // When
        paperReplicator.replicatePapers(1000, 100);

        // Then
        ArgumentCaptor<List<Paper>> paperArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paperRepository, times(2)).save(paperArgumentCaptor.capture());

        List<Paper> firstBatchOfSavedPapers = paperArgumentCaptor.getAllValues().get(0);
        assertThat(firstBatchOfSavedPapers, hasSize(1));
        assertThat(firstBatchOfSavedPapers.get(0).getId(), is(equalTo("101")));

        List<Paper> secondBatchOfSavedPapers = paperArgumentCaptor.getAllValues().get(1);
        assertThat(secondBatchOfSavedPapers, hasSize(2));
        assertThat(secondBatchOfSavedPapers.get(0).getId(), is(equalTo("101")));
        assertThat(secondBatchOfSavedPapers.get(1).getId(), is(equalTo("102")));
    }

    public static class InMemoryEntityFeedJobCheckpointer implements FeedJobCheckpointer<Paper> {
        private Optional<String> lastProcessedPaperId = Optional.empty();

        @Override
        public void checkpoint(Paper paper) {
            lastProcessedPaperId = Optional.of(paper.getId());
        }

        @Override
        public Optional<String> getLastCheckpoint() {
            return lastProcessedPaperId;
        }
    }
}
