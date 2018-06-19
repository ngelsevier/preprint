package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.domain.Paper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static com.ssrn.search.domain.SubmissionStage.*;
import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.AuthorUpdateBuilder.anAuthorUpdate;
import static com.ssrn.search.shared.PaperBuilder.aPaper;
import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static com.ssrn.search.shared.SearchIndexPaperBuilder.aSearchIndexPaper;
import static com.ssrn.search.shared.matchers.mockito.CollectionContainingOnlyMatcher.collectionContainingOnly;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorIdMatcher.authorId;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorMatcher.authorWith;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorNameMatcher.authorName;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorsInOrderMatcher.authorsInOrder;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperIdMatcher.paperId;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperKeywordsMatcher.paperKeywords;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperMatcher.searchIndexPaperWith;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperTitleMatcher.paperTitle;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class LibrarianTest {

    private static final AuthorUpdate.Author[] ANY_AUTHORS = new AuthorUpdate.Author[0];
    private static final SearchIndexPaper[] ANY_SEARCH_INDEX_PAPERS = new SearchIndexPaper[0];

    @Test
    public void shouldUpdateEachPaperInSearchIndexWithItsAuthors() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        AuthorUpdate.Author[] authors = {
                anAuthor().withId("103").withName("Tom").build(),
                anAuthor().withId("102").withName("Dick").build(),
                anAuthor().withId("101").withName("Harry").build()
        };

        when(authorRegistry.getByIds(collectionContainingOnly("103", "101", "102"))).thenReturn(authors);

        Paper firstPaper = aPaper()
                .withId("1")
                .withTitle("First Paper")
                .withKeywords("random string")
                .withAuthorIds("101", "102")
                .withSubmissionStage(SUBMITTED)
                .build();

        Paper secondPaper = aPaper()
                .withId("2")
                .withTitle("Second Paper")
                .withAuthorIds("103")
                .withSubmissionStage(SUBMITTED)
                .build();

        Paper thirdPaper = aPaper()
                .withId("3")
                .withTitle("Third Paper")
                .withAuthorIds("102")
                .withSubmissionStage(SUBMITTED)
                .build();

        List<Paper> batchOfPapers = asList(firstPaper, secondPaper, thirdPaper);

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        ArgumentCaptor<List<SearchIndexPaper>> paperArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).update(paperArgumentCaptor.capture());
        List<SearchIndexPaper> papersIndexedInElasticsearch = paperArgumentCaptor.getValue();
        assertThat(papersIndexedInElasticsearch, hasSize(3));

        assertThat(papersIndexedInElasticsearch.get(0), is(searchIndexPaperWith(
                paperId("1"),
                paperTitle("First Paper"),
                paperKeywords("random string"),
                authorsInOrder(
                        authorWith(authorId("101"), authorName("Harry")),
                        authorWith(authorId("102"), authorName("Dick"))
                )
        )));

        assertThat(papersIndexedInElasticsearch.get(1), is(searchIndexPaperWith(
                paperId("2"),
                paperTitle("Second Paper"),
                paperKeywords(null),
                authorsInOrder(authorWith(authorId("103"), authorName("Tom")))
        )));

        assertThat(papersIndexedInElasticsearch.get(2), is(searchIndexPaperWith(
                paperId("3"),
                paperTitle("Third Paper"),
                paperKeywords(null),
                authorsInOrder(authorWith(authorId("102"), authorName("Dick")))
        )));
    }

    @Test
    public void shouldOnlyLookupAuthorsOncePerBatchOfPapers() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        when(authorRegistry.getByIds(collectionContainingOnly("1", "2")))
                .thenReturn(ANY_AUTHORS);

        List<Paper> batchOfPapers = asList(
                aPaper().withAuthorIds("1").withSubmissionStage(SUBMITTED).build(),
                aPaper().withAuthorIds("2").withSubmissionStage(SUBMITTED).build()
        );

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        verify(authorRegistry, times(1)).getByIds(asList("1", "2"));
    }

    @Test
    public void shouldUpdatePapersInLibraryInASingleBatch() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        when(authorRegistry.getByIds(any())).thenReturn(ANY_AUTHORS);

        List<Paper> batchOfPapers = asList(aPaper().withSubmissionStage(SUBMITTED).build(), aPaper().withSubmissionStage(SUBMITTED).build());

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        verify(library, times(1)).update(any());
    }

    @Test
    public void shouldDeletePapersMarkedPrivate() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        List<Paper> batchOfPapers = asList(
                aPaper().withId("1").isPrivate().withSubmissionStage(SUBMITTED).build(),
                aPaper().withId("2").isPublic().withSubmissionStage(SUBMITTED).build(),
                aPaper().withId("3").isPrivate().withSubmissionStage(SUBMITTED).build());

        when(authorRegistry.getByIds(any())).thenReturn(ANY_AUTHORS);

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        ArgumentCaptor<List<String>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).delete(argumentCaptor.capture());

        List<String> papersDeletedInLibrary = argumentCaptor.getValue();
        assertThat(papersDeletedInLibrary, hasSize(2));

        assertThat(papersDeletedInLibrary.get(0), is(equalTo("1")));
        assertThat(papersDeletedInLibrary.get(1), is(equalTo("3")));
    }

    @Test
    public void shouldDeletePapersMarkedIrrelevant() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        List<Paper> batchOfPapers = asList(
                aPaper().withId("1").isIrrelevant().withSubmissionStage(SUBMITTED).build(),
                aPaper().withId("2").isRelevant().withSubmissionStage(SUBMITTED).build(),
                aPaper().withId("3").isIrrelevant().withSubmissionStage(SUBMITTED).build());

        when(authorRegistry.getByIds(any())).thenReturn(ANY_AUTHORS);

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        ArgumentCaptor<List<String>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).delete(argumentCaptor.capture());

        List<String> papersDeletedInLibrary = argumentCaptor.getValue();
        assertThat(papersDeletedInLibrary, hasSize(2));

        assertThat(papersDeletedInLibrary.get(0), is(equalTo("1")));
        assertThat(papersDeletedInLibrary.get(1), is(equalTo("3")));
    }

    @Test
    public void shouldDeletePapersMarkedRestricted() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        List<Paper> batchOfPapers = asList(
                aPaper().withId("1").isRelevant().isRestricted().withSubmissionStage(APPROVED).build(),
                aPaper().withId("2").isRelevant().withSubmissionStage(APPROVED).build(),
                aPaper().withId("3").isRelevant().isRestricted().withSubmissionStage(APPROVED).build());

        when(authorRegistry.getByIds(any())).thenReturn(ANY_AUTHORS);

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        ArgumentCaptor<List<String>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).delete(argumentCaptor.capture());

        List<String> papersDeletedInLibrary = argumentCaptor.getValue();
        assertThat(papersDeletedInLibrary, hasSize(2));

        assertThat(papersDeletedInLibrary.get(0), is(equalTo("1")));
        assertThat(papersDeletedInLibrary.get(1), is(equalTo("3")));
    }

    @Test
    public void shouldDeletePapersInNonSearchableStages() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        List<Paper> batchOfPapers = asList(
                aPaper().withId("1").withSubmissionStage(IN_DRAFT).build(),
                aPaper().withId("2").withSubmissionStage(DELETED).build(),
                aPaper().withId("3").withSubmissionStage(REJECTED).build(),
                aPaper().withId("4").withSubmissionStage(UNDER_REVIEW).build(),
                aPaper().withId("5").withSubmissionStage(APPROVED).build());

        when(authorRegistry.getByIds(any())).thenReturn(ANY_AUTHORS);

        // When
        librarian.updatePapers(batchOfPapers);

        // Then
        ArgumentCaptor<List<String>> deleteArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).delete(deleteArgumentCaptor.capture());
        ArgumentCaptor<List<SearchIndexPaper>> updateArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).update(updateArgumentCaptor.capture());

        List<String> papersDeletedInLibrary = deleteArgumentCaptor.getValue();
        assertThat(papersDeletedInLibrary, hasSize(3));

        assertThat(papersDeletedInLibrary.get(0), is(equalTo("1")));
        assertThat(papersDeletedInLibrary.get(1), is(equalTo("2")));
        assertThat(papersDeletedInLibrary.get(2), is(equalTo("3")));

        List<SearchIndexPaper> papersUpdatedInLibrary = updateArgumentCaptor.getValue();
        assertThat(papersUpdatedInLibrary, hasSize(2));

        assertThat(papersUpdatedInLibrary.get(0).getId(), is(equalTo("4")));
        assertThat(papersUpdatedInLibrary.get(1).getId(), is(equalTo("5")));
    }

    @Test
    public void shouldUpdateExistingPapersWhenNotifiedThatAuthorsHaveBeenUpdated() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        AuthorUpdate.Author author103 = anAuthor().withId("103").withName("Fred Perry").build();
        AuthorUpdate.Author author101 = anAuthor().withId("101").withName("John Terry").build();

        when(library.getPapersWrittenBy(collectionContainingOnly(author101, author103)))
                .thenReturn(
                        Arrays.stream(
                                new SearchIndexPaper[]{
                                        aSearchIndexPaper()
                                                .withId("1")
                                                .withTitle("First Paper")
                                                .withAuthors(
                                                        aSearchIndexPaperAuthor().withId("103").withName("Billy Bob"),
                                                        aSearchIndexPaperAuthor().withId("101").withNoName()
                                                ).build(),
                                        aSearchIndexPaper()
                                                .withId("2")
                                                .withTitle("Second Paper")
                                                .withAuthors(
                                                        aSearchIndexPaperAuthor().withId("103").withName("John Smith")
                                                ).build()
                                }
                        )
                );

        List<AuthorUpdate> batchOfAuthorUpdates = asList(
                anAuthorUpdate().withAuthor(author103).build(),
                anAuthorUpdate().withAuthor(author101).build()
        );

        // When
        librarian.applyAuthorUpdates(batchOfAuthorUpdates);

        // Then
        ArgumentCaptor<List<SearchIndexPaper>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(library).update(argumentCaptor.capture());

        List<SearchIndexPaper> papersUpdatedInLibrary = argumentCaptor.getValue();
        assertThat(papersUpdatedInLibrary, hasSize(2));

        assertThat(papersUpdatedInLibrary.get(0), is(searchIndexPaperWith(
                paperId("1"),
                paperTitle("First Paper"),
                authorsInOrder(
                        authorWith(authorId("103"), authorName("Fred Perry")),
                        authorWith(authorId("101"), authorName("John Terry"))
                )
        )));

        assertThat(papersUpdatedInLibrary.get(1), is(searchIndexPaperWith(
                paperId("2"),
                paperTitle("Second Paper"),
                authorsInOrder(
                        authorWith(authorId("103"), authorName("Fred Perry"))
                )
        )));
    }

    @Test
    public void shouldRetrieveAffectedPapersInASingleBatchWhenNotifiedThatAuthorsHaveBeenUpdated() {
        // Given
        Library library = mock(Library.class);
        AuthorRegistry authorRegistry = mock(AuthorRegistry.class);
        Librarian librarian = new Librarian(library, authorRegistry);

        AuthorUpdate.Author anAuthor = anAuthor().build();

        when(library.getPapersWrittenBy(any()))
                .thenReturn(Arrays.stream(ANY_SEARCH_INDEX_PAPERS));

        List<AuthorUpdate> batchOfAuthorUpdates = singletonList(anAuthorUpdate().withAuthor(anAuthor).build());

        // When
        librarian.applyAuthorUpdates(batchOfAuthorUpdates);

        // Then
        verify(library, times(1)).getPapersWrittenBy(any());
    }
}
