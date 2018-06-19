package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.domain.Paper;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.PaperBuilder.aPaper;
import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static com.ssrn.search.shared.SearchIndexPaperBuilder.aSearchIndexPaper;
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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PaperIndexClerkTest {
    @Test
    public void shouldCollatePapersAndAuthorsIntoIndexablePapers() {
        // Given
        PaperIndexClerk paperIndexClerk = new PaperIndexClerk();

        List<Paper> papers = asList(
                aPaper().withId("1").withTitle("First").withKeywords("random string").withAuthorIds("1", "2").build(),
                aPaper().withId("2").withTitle("Second").withAuthorIds("2", "3").build()
        );

        AuthorUpdate.Author[] authors = {
                anAuthor().withId("1").withName("Author 1").build(),
                anAuthor().withId("2").withName("Author 2").build(),
                anAuthor().withId("3").withName("Author 3").build()
        };

        // When
        List<SearchIndexPaper> searchIndexPapers = paperIndexClerk.prepareIndexablePapers(papers, authors);

        // Then
        assertThat(searchIndexPapers, hasSize(2));
        assertThat(searchIndexPapers, hasItems(
                searchIndexPaperWith(
                        paperId("1"),
                        paperTitle("First"),
                        paperKeywords("random string"),
                        authorsInOrder(
                                authorWith(authorId("1"), authorName("Author 1")),
                                authorWith(authorId("2"), authorName("Author 2"))
                        )
                ),
                searchIndexPaperWith(
                        paperId("2"),
                        paperTitle("Second"),
                        paperKeywords(null),
                        authorsInOrder(
                                authorWith(authorId("2"), authorName("Author 2")),
                                authorWith(authorId("3"), authorName("Author 3"))
                        )
                )
        ));
    }

    @Test
    public void shouldMaintainTheOrderOfAuthorsOnAPaper() {
        // Given
        PaperIndexClerk paperIndexClerk = new PaperIndexClerk();

        List<Paper> papers = singletonList(
                aPaper().withId("1").withTitle("First").withAuthorIds("1", "2").build()
        );

        AuthorUpdate.Author[] authors = {
                anAuthor().withId("2").withName("Author 2").build(),
                anAuthor().withId("1").withName("Author 1").build()
        };

        // When
        List<SearchIndexPaper> searchIndexPapers = paperIndexClerk.prepareIndexablePapers(papers, authors);

        // Then
        assertThat(searchIndexPapers, hasItems(
                searchIndexPaperWith(authorsInOrder(authorWith(authorId("1")), authorWith(authorId("2")))))
        );
    }

    @Test
    public void shouldUseNullAuthorNameWhenAnAuthorIsUnavailable() {
        // Given
        PaperIndexClerk paperIndexClerk = new PaperIndexClerk();

        List<Paper> papers = singletonList(
                aPaper().withId("1").withTitle("First").withAuthorIds("1").build()
        );

        AuthorUpdate.Author[] authors = new AuthorUpdate.Author[0];

        // When
        List<SearchIndexPaper> searchIndexPapers = paperIndexClerk.prepareIndexablePapers(papers, authors);

        // Then
        assertThat(searchIndexPapers, hasItems(
                searchIndexPaperWith(authorsInOrder(authorWith(authorId("1"), authorName(null)))))
        );
    }

    @Test
    public void shouldPrepareIndexablePapersRequiringUpdateDueToStaleAuthorName() {
        // Given
        Stream<SearchIndexPaper> papers = Stream.of(
                aSearchIndexPaper()
                        .withId("A")
                        .withTitle("Paper A")
                        .withAuthors(
                                aSearchIndexPaperAuthor().withId("2").withName("Initial Name 2"),
                                aSearchIndexPaperAuthor().withId("1").withName("Up To Date Name 1")
                        )
                        .build(),
                aSearchIndexPaper()
                        .withId("B")
                        .withAuthors(aSearchIndexPaperAuthor().withId("1").withName("Initial Name 1"))
                        .build(),
                aSearchIndexPaper()
                        .withId("C")
                        .withAuthors(aSearchIndexPaperAuthor().withId("3").withNoName())
                        .build(),
                aSearchIndexPaper()
                        .withId("D")
                        .withAuthors(aSearchIndexPaperAuthor().withId("3").withName("Up To Date Name 3"))
                        .build()
        );

        List<AuthorUpdate.Author> updatedAuthors = asList(
                anAuthor().withId("1").withName("Up To Date Name 1").build(),
                anAuthor().withId("2").withName("Up To Date Name 2").build(),
                anAuthor().withId("3").withName("Up To Date Name 3").build()
        );

        PaperIndexClerk paperIndexClerk = new PaperIndexClerk();

        // When
        List<SearchIndexPaper> indexablePapersRequiringUpdate = paperIndexClerk.prepareIndexablePapersRequiringUpdate(papers, updatedAuthors);

        // Then
        assertThat(indexablePapersRequiringUpdate,
                hasItems(
                        searchIndexPaperWith(
                                paperId("A"),
                                paperTitle("Paper A"),
                                authorsInOrder(
                                        authorWith(authorId("2"), authorName("Up To Date Name 2")),
                                        authorWith(authorId("1"), authorName("Up To Date Name 1"))
                                )
                        ),
                        searchIndexPaperWith(
                                paperId("B"),
                                authorsInOrder(authorWith(authorId("1"), authorName("Up To Date Name 1")))
                        ),
                        searchIndexPaperWith(
                                paperId("C"),
                                authorsInOrder(authorWith(authorId("3"), authorName("Up To Date Name 3")))
                        )
                )
        );

        assertThat(indexablePapersRequiringUpdate, not(hasItem(searchIndexPaperWith(paperId("D")))));
    }
}