package com.ssrn.search.shared;

import com.ssrn.search.shared.matchers.search_index_paper.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static com.ssrn.search.shared.SearchIndexPaperBuilder.aSearchIndexPaper;
import static com.ssrn.search.shared.matchers.SearchResultMatcher.searchResultWith;
import static com.ssrn.search.shared.matchers.SearchResultIdMatcher.id;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorIdMatcher.authorId;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorMatcher.authorWith;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorNameMatcher.authorName;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorsInOrderMatcher.authorsInOrder;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperTitleMatcher.paperTitle;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertThat;

public class ElasticsearchPapersWrittenByAuthorsScrollIteratorTest extends ElasticsearchTest {

    private static final String PAPER_DOCUMENT_TYPE = "paper";
    private static final int ANY_SCROLL_SIZE = 1;
    private ElasticsearchClient elasticsearchClient;
    private static final String[] ANY_AUTHOR_IDS = new String[]{"1"};

    @Before
    public void before() {
        elasticsearchClient = new ElasticsearchClient(elasticsearchCluster().nodeHostname(), elasticsearchCluster().nodePort());
    }

    @Test
    public void shouldIntiallyIndicateThereAreMoreItemsToIterate() {
        ElasticsearchPapersWrittenByAuthorsScrollIterator scrollIterator = new ElasticsearchPapersWrittenByAuthorsScrollIterator(
                ANY_AUTHOR_IDS, ELASTICSEARCH_PAPERS_INDEX_NAME, PAPER_DOCUMENT_TYPE, elasticsearchClient, ANY_SCROLL_SIZE
        );


        assertThat(scrollIterator.hasNext(), is(equalTo(true)));
    }

    @Test
    public void shouldIndicateWhenThereAreMoreItemsToIterate() {
        ElasticsearchPapersWrittenByAuthorsScrollIterator scrollIterator = new ElasticsearchPapersWrittenByAuthorsScrollIterator(
                new String[]{"1"}, ELASTICSEARCH_PAPERS_INDEX_NAME, PAPER_DOCUMENT_TYPE, elasticsearchClient, ANY_SCROLL_SIZE
        );

        assertThat(scrollIterator.hasNext(), is(equalTo(true)));

        scrollIterator.next();

        assertThat(scrollIterator.hasNext(), is(equalTo(false)));
    }

    @Test
    public void shouldProvideAllPapersWrittenByASpecifiedAuthor() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                ANY_SCROLL_SIZE);

        SearchIndexPaperAuthor author = aSearchIndexPaperAuthor().withId("1").withName("Tom").build();

        String uniqueString = getRandomString();

        SearchIndexPaper firstPaper = aSearchIndexPaper()
                .withId("A")
                .withTitle(String.format("Paper A %s", uniqueString))
                .withAuthors(author, aSearchIndexPaperAuthor().withId("2").withName("Jim").build())
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withId("B")
                .withTitle(String.format("Paper B %s", uniqueString))
                .withAuthors(author)
                .build();

        library.update(asList(firstPaper, secondPaper));

        assertThat(() -> library.searchForItemsMatching(uniqueString, 0, 10).getResults(),
                eventuallySatisfies(arrayContainingInAnyOrder(
                        searchResultWith(id("A")),
                        searchResultWith(id("B"))
                )).within(3, SECONDS, checkingEvery(100, MILLISECONDS)));

        ElasticsearchPapersWrittenByAuthorsScrollIterator scrollIterator = new ElasticsearchPapersWrittenByAuthorsScrollIterator(
                new String[]{"1"}, ELASTICSEARCH_PAPERS_INDEX_NAME, PAPER_DOCUMENT_TYPE, elasticsearchClient, 5
        );

        // When
        SearchIndexPaper[] searchIndexPapers = scrollIterator.next();

        // Then
        assertThat(searchIndexPapers, is(arrayWithSize(2)));
        assertThat(searchIndexPapers, arrayContainingInAnyOrder(
                SearchIndexPaperMatcher.searchIndexPaperWith(
                        SearchIndexPaperIdMatcher.paperId("A"),
                        paperTitle(firstPaper.getTitle()),
                        authorsInOrder(
                                authorWith(authorId("1"), authorName("Tom")),
                                authorWith(authorId("2"), authorName("Jim"))
                        )
                ),
                SearchIndexPaperMatcher.searchIndexPaperWith(
                        SearchIndexPaperIdMatcher.paperId("B"),
                        paperTitle(secondPaper.getTitle()),
                        authorsInOrder(authorWith(authorId("1"), authorName("Tom")))
                )
        ));
    }

    @Test
    public void shouldSupportNotFindingAnyPapersForSpecifiedAuthor() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                ANY_SCROLL_SIZE);

        String uniqueString = getRandomString();

        SearchIndexPaper firstPaper = aSearchIndexPaper()
                .withId("A")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("1").withName("Tom").build())
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withId("B")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("2").withName("Jim").build())
                .build();

        library.update(asList(firstPaper, secondPaper));

        assertThat(() -> library.searchForItemsMatching(uniqueString, 0, 10).getResults(),
                eventuallySatisfies(arrayContainingInAnyOrder(
                        searchResultWith(id("A")),
                        searchResultWith(id("B"))
                )).within(3, SECONDS, checkingEvery(100, MILLISECONDS)));

        ElasticsearchPapersWrittenByAuthorsScrollIterator scrollIterator = new ElasticsearchPapersWrittenByAuthorsScrollIterator(
                new String[]{"1"}, ELASTICSEARCH_PAPERS_INDEX_NAME, PAPER_DOCUMENT_TYPE, elasticsearchClient, 5
        );

        // When
        SearchIndexPaper[] searchIndexPapers = scrollIterator.next();

        // Then
        assertThat(searchIndexPapers, is(arrayWithSize(1)));
        assertThat(searchIndexPapers, arrayContaining(
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("A"))
        ));
    }

    @Test
    public void shouldProvideAllPapersWrittenByAllSpecifiedAuthors() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                ANY_SCROLL_SIZE
        );

        SearchIndexPaperAuthor tom = aSearchIndexPaperAuthor().withId("1").withName("Tom").build();
        SearchIndexPaperAuthor jim = aSearchIndexPaperAuthor().withId("2").withName("Jim").build();

        String uniqueString = getRandomString();

        SearchIndexPaper firstPaper = aSearchIndexPaper()
                .withId("A")
                .withTitle(uniqueString)
                .withAuthors(tom)
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withId("B")
                .withTitle(uniqueString)
                .withAuthors(jim)
                .build();

        SearchIndexPaper thirdPaper = aSearchIndexPaper()
                .withId("C")
                .withTitle(uniqueString)
                .withAuthors(tom, jim)
                .build();

        library.update(asList(firstPaper, secondPaper, thirdPaper));

        assertThat(() -> library.searchForItemsMatching(uniqueString, 0, 10).getResults(),
                eventuallySatisfies(arrayContainingInAnyOrder(
                        searchResultWith(id("A")),
                        searchResultWith(id("B")),
                        searchResultWith(id("C"))
                )).within(3, SECONDS, checkingEvery(100, MILLISECONDS)));

        ElasticsearchPapersWrittenByAuthorsScrollIterator scrollIterator = new ElasticsearchPapersWrittenByAuthorsScrollIterator(
                new String[]{"1", "2"}, ELASTICSEARCH_PAPERS_INDEX_NAME, PAPER_DOCUMENT_TYPE, elasticsearchClient, 5
        );

        // When
        SearchIndexPaper[] searchIndexPapers = scrollIterator.next();

        // Then
        assertThat(searchIndexPapers, is(arrayWithSize(3)));
        assertThat(searchIndexPapers, arrayContainingInAnyOrder(
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("A")),
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("B")),
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("C"))
        ));
    }

    @Test
    public void shouldProvideAllPapersReturnedByElasticsearchAcrossMultiplePages() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                ANY_SCROLL_SIZE);

        String uniqueString = getRandomString();

        SearchIndexPaper firstPaper = aSearchIndexPaper()
                .withId("A")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("1"))
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withId("B")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("2"))
                .build();

        SearchIndexPaper thirdPaper = aSearchIndexPaper()
                .withId("C")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("3"))
                .build();

        SearchIndexPaper fourthPaper = aSearchIndexPaper()
                .withId("D")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("4"))
                .build();

        SearchIndexPaper fifthPaper = aSearchIndexPaper()
                .withId("E")
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withId("5"))
                .build();


        library.update(asList(firstPaper, secondPaper, thirdPaper, fourthPaper, fifthPaper));

        assertThat(() -> library.searchForItemsMatching(uniqueString, 0, 10).getResults(),
                eventuallySatisfies(arrayContainingInAnyOrder(
                        searchResultWith(id("A")),
                        searchResultWith(id("B")),
                        searchResultWith(id("C")),
                        searchResultWith(id("D")),
                        searchResultWith(id("E"))
                )).within(3, SECONDS, checkingEvery(100, MILLISECONDS)));

        ElasticsearchPapersWrittenByAuthorsScrollIterator scrollIterator = new ElasticsearchPapersWrittenByAuthorsScrollIterator(
                new String[]{"1", "2", "3", "4", "5"}, ELASTICSEARCH_PAPERS_INDEX_NAME, PAPER_DOCUMENT_TYPE, elasticsearchClient, 2
        );

        // When
        SearchIndexPaper[] firstIteration = scrollIterator.next();

        // Then
        assertThat(firstIteration, is(arrayWithSize(2)));
        assertThat(scrollIterator.hasNext(), is(equalTo(true)));

        // When
        SearchIndexPaper[] secondIteration = scrollIterator.next();

        // Then
        assertThat(secondIteration, is(arrayWithSize(2)));
        assertThat(scrollIterator.hasNext(), is(equalTo(true)));

        // When
        SearchIndexPaper[] thirdIteration = scrollIterator.next();

        // Then
        assertThat(thirdIteration, is(arrayWithSize(1)));
        assertThat(scrollIterator.hasNext(), is(equalTo(true)));

        // When
        SearchIndexPaper[] fourthIteration = scrollIterator.next();

        // Then
        assertThat(fourthIteration, is(arrayWithSize(0)));
        assertThat(scrollIterator.hasNext(), is(equalTo(false)));

        SearchIndexPaper[] iteratedPapers = Stream.of(firstIteration, secondIteration, thirdIteration)
                .flatMap(Arrays::stream)
                .toArray(SearchIndexPaper[]::new);

        assertThat(iteratedPapers, arrayContainingInAnyOrder(
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("A")),
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("B")),
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("C")),
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("D")),
                SearchIndexPaperMatcher.searchIndexPaperWith(SearchIndexPaperIdMatcher.paperId("E"))
        ));
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString();
    }

}