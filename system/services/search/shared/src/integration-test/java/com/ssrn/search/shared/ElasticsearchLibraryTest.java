package com.ssrn.search.shared;

import com.ssrn.search.domain.AuthorUpdate;
import com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorIdMatcher;
import com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorNameMatcher;
import com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperKeywordsMatcher;
import com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperTitleMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ssrn.search.shared.AuthorBuilder.anAuthor;
import static com.ssrn.search.shared.SearchIndexPaperAuthorBuilder.aSearchIndexPaperAuthor;
import static com.ssrn.search.shared.SearchIndexPaperBuilder.aSearchIndexPaper;
import static com.ssrn.search.shared.matchers.AuthorSearchResultNameMatcher.name;
import static com.ssrn.search.shared.matchers.SearchResultAuthorIdMatcher.authorId;
import static com.ssrn.search.shared.matchers.SearchResultAuthorMatcher.paperAuthor;
import static com.ssrn.search.shared.matchers.SearchResultAuthorNameMatcher.authorName;
import static com.ssrn.search.shared.matchers.SearchResultAuthorsMatcher.paperAuthors;
import static com.ssrn.search.shared.matchers.SearchResultIdMatcher.id;
import static com.ssrn.search.shared.matchers.SearchResultKeywordsMatcher.paperKeywords;
import static com.ssrn.search.shared.matchers.SearchResultMatcher.searchResultWith;
import static com.ssrn.search.shared.matchers.SearchResultTitleMatcher.paperTitle;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorMatcher.authorWith;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperAuthorsInOrderMatcher.authorsInOrder;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperIdMatcher.paperId;
import static com.ssrn.search.shared.matchers.search_index_paper.SearchIndexPaperMatcher.searchIndexPaperWith;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.matchers.MatchesAllOf.matchesAllOf;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.ThreadingUtils.sleepFor;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.collection.IsArrayWithSize.emptyArray;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class ElasticsearchLibraryTest extends ElasticsearchTest {

    @Test
    public void shouldFindPapersContainingSearchTermInTitle() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueString = getRandomString();
        String expectedHighlightedUniqueString = String.format("<em>%s</em>", uniqueString);

        SearchIndexPaper firstPaperWithSearchTermInTitle = aSearchIndexPaper()
                .withTitle(String.format("%s 1", uniqueString))
                .withAuthors(aSearchIndexPaperAuthor().withId("2").withName("Matt"), aSearchIndexPaperAuthor().withId("3").withName("Erik"))
                .build();

        SearchIndexPaper secondPaperWithSearchTermInTitle = aSearchIndexPaper()
                .withTitle(String.format("%s 2", uniqueString))
                .withAuthors(aSearchIndexPaperAuthor().withId("5").withName("Trupti"))
                .build();

        SearchIndexPaper paperWithoutSearchTermInTitle = aSearchIndexPaper()
                .withTitle(String.format("%s 3", getRandomString()))
                .withAuthors(aSearchIndexPaperAuthor().withId("7").withNoName())
                .build();

        library.update(asList(firstPaperWithSearchTermInTitle, secondPaperWithSearchTermInTitle, paperWithoutSearchTermInTitle));

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 0, 50).getResults(),
                // Then
                eventuallySatisfies(matchesAllOf(
                        arrayWithSize(2),
                        not(hasItemInArray(is(searchResultWith(id(paperWithoutSearchTermInTitle.getId()))))),
                        listOf(paper -> hasItemInArray(is(searchResultWith(matchesAllOf(
                                id(paper.getId()),
                                paperTitle(paper.getTitle().replace(uniqueString, expectedHighlightedUniqueString)),
                                paperAuthors(matchesAllOf(
                                        listOf(author -> hasItemInArray(paperAuthor(matchesAllOf(
                                                authorId(author.getId()),
                                                authorName(author.getName())
                                        ))), paper.getAuthors())
                                ))
                        )))), firstPaperWithSearchTermInTitle, secondPaperWithSearchTermInTitle)
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldFindPapersContainingSearchTermInKeywords() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueString = getRandomString();
        String expectedHighlightedUniqueString = String.format("<em>%s</em>", uniqueString);

        SearchIndexPaper firstPaperWithSearchTermInKeyword = aSearchIndexPaper()
                .withTitle(getRandomString())
                .withKeywords(String.format("random, string, %s", uniqueString))
                .build();

        SearchIndexPaper secondPaperWithRandomTitle = aSearchIndexPaper().build();

        library.update(asList(firstPaperWithSearchTermInKeyword, secondPaperWithRandomTitle));

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 0, 1).getResults(),
                // Then
                eventuallySatisfies(matchesAllOf(
                        arrayWithSize(1),
                        not(hasItemInArray(is(searchResultWith(id(secondPaperWithRandomTitle.getId()))))),
                        hasItemInArray(is(searchResultWith(matchesAllOf(
                                id(firstPaperWithSearchTermInKeyword.getId()),
                                paperTitle(firstPaperWithSearchTermInKeyword.getTitle()),
                                paperKeywords(firstPaperWithSearchTermInKeyword.getKeywords().replace(uniqueString, expectedHighlightedUniqueString)),
                                paperAuthors(matchesAllOf(
                                        listOf(author -> hasItemInArray(paperAuthor(matchesAllOf(
                                                authorId(author.getId()),
                                                authorName(author.getName())
                                        ))), firstPaperWithSearchTermInKeyword.getAuthors())
                                ))
                        ))))
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldFindPapersAndAuthorsContainingSearchTermInAuthorName() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        ElasticsearchAuthorRegistry registry = new ElasticsearchAuthorRegistry(
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort()
        );

        String uniqueAuthorNameString = getRandomString();
        String expectedHighlightedAuthorNameString = String.format("<em>%s</em>", uniqueAuthorNameString);

        String uniquePaperTitle = String.format("Paper 1 %s", getRandomString());
        SearchIndexPaper firstPaperWithSearchTermInAuthorName = aSearchIndexPaper()
                .withTitle(uniquePaperTitle)
                .withAuthors(aSearchIndexPaperAuthor().withId("2").withName(uniqueAuthorNameString), aSearchIndexPaperAuthor().withId("3").withName("Erik"))
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withTitle(String.format("Paper 2 %s", getRandomString()))
                .withAuthors(aSearchIndexPaperAuthor().withId("5").withName("Trupti"))
                .build();

        AuthorUpdate.Author authorWithSearchTermInName = anAuthor().withId("2").withName(uniqueAuthorNameString).build();
        AuthorUpdate.Author authorWithoutSearchTermInName = anAuthor().withName(getRandomString()).build();

        library.update(asList(firstPaperWithSearchTermInAuthorName, secondPaper));
        registry.update(asList(authorWithSearchTermInName, authorWithoutSearchTermInName));

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueAuthorNameString, 0, 50).getResults(),
                // Then
                eventuallySatisfies(matchesAllOf(
                        arrayWithSize(2),
                        not(hasItemInArray(is(searchResultWith(id(secondPaper.getId()))))),
                        not(hasItemInArray(is(searchResultWith(id(authorWithoutSearchTermInName.getId()))))),
                        hasItemInArray(is(searchResultWith(
                                matchesAllOf(
                                        id("2"),
                                        name(expectedHighlightedAuthorNameString))
                        ))),
                        hasItemInArray(is(searchResultWith(
                                matchesAllOf(
                                        id(firstPaperWithSearchTermInAuthorName.getId()),
                                        paperTitle(uniquePaperTitle),
                                        paperAuthors(matchesAllOf(
                                                listOf(author -> hasItemInArray(paperAuthor(matchesAllOf(
                                                        authorId(author.getId()),
                                                        authorName(author.getName().replace(uniqueAuthorNameString, expectedHighlightedAuthorNameString))
                                                ))), firstPaperWithSearchTermInAuthorName.getAuthors())
                                        ))
                                )
                        )))
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldSupportPapersWithUnnamedAuthors() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueString = getRandomString();

        SearchIndexPaper aPaper = aSearchIndexPaper()
                .withTitle(uniqueString)
                .withAuthors(aSearchIndexPaperAuthor().withNoName())
                .build();

        library.update(asList(aPaper));

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 0, 1).getResults(),
                // Then
                eventuallySatisfies(hasItemInArray(is(searchResultWith(matchesAllOf(
                        id(aPaper.getId()),
                        paperAuthors(matchesAllOf(
                                listOf(author -> hasItemInArray(paperAuthor(matchesAllOf(
                                        authorId(author.getId()),
                                        authorName(null)
                                ))), aPaper.getAuthors())
                        ))
                ))))).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldDeletePapersThatAreNoLongerSearchable() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueString = getRandomString();
        SearchIndexPaper paper1 = aSearchIndexPaper()
                .withTitle(String.format("SearchIndexPaper 1 %s", uniqueString))
                .build();

        SearchIndexPaper paper2 = aSearchIndexPaper()
                .withTitle(String.format("SearchIndexPaper 2 %s", uniqueString))
                .build();

        SearchIndexPaper paper3 = aSearchIndexPaper()
                .withTitle(String.format("SearchIndexPaper 3 %s", uniqueString))
                .build();

        library.update(asList(paper1, paper2, paper3));

        // Guard-assertion
        assertThat(
                () -> library.searchForItemsMatching(uniqueString, 0, 50).getResults(),
                eventuallySatisfies(matchesAllOf(
                        arrayWithSize(3),
                        listOf(paper -> hasItemInArray(is(searchResultWith(matchesAllOf(id(paper.getId()))))), paper1, paper2)
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );

        // When
        library.delete(asList(paper1.getId(), paper2.getId()));

        sleepFor(2, SECONDS);

        // Then

        assertThat(
                () -> library.searchForItemsMatching(uniqueString, 0, 50).getResults(),
                eventuallySatisfies(matchesAllOf(
                        not(hasItemInArray(is(searchResultWith(matchesAllOf(id(paper1.getId())))))),
                        not(hasItemInArray(is(searchResultWith(matchesAllOf(id(paper2.getId())))))),
                        hasItemInArray(is(searchResultWith(matchesAllOf(id(paper3.getId())))))
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldNotAttemptToDeletePapersIfEmptyBatchOfPapersProvided() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        // When
        library.delete(new ArrayList<>());

        // Then, no exception is thrown
    }

    @Test
    public void shouldUpdatePaperTitleWhenAddingAPaperThatAlreadyExists() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );


        SearchIndexPaper paper = aSearchIndexPaper()
                .withTitle(String.format("Initial SearchIndexPaper Title %s", getRandomString()))
                .build();

        library.update(singletonList(paper));

        String uniqueString = getRandomString();
        String expectedHighlightedUniqueString = String.format("<em>%s</em>", uniqueString);
        SearchIndexPaper updatedPaper = aSearchIndexPaper()
                .withId(paper.getId())
                .withTitle(String.format("Updated SearchIndexPaper Title %s", uniqueString))
                .build();

        // When
        library.update(singletonList(updatedPaper));

        // Then
        assertThat(
                () -> library.searchForItemsMatching(uniqueString, 0, 1).getResults(),
                eventuallySatisfies(allOf(
                        is(arrayWithSize(1)),
                        hasItemInArray(is(searchResultWith(matchesAllOf(id(paper.getId()), paperTitle(updatedPaper.getTitle().replace(uniqueString, expectedHighlightedUniqueString))))))
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );

    }

    @Test
    public void shouldNotAttemptToExecuteUpdateIfEmptyBatchOfPapersProvided() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        // When
        library.update(Collections.emptyList());

        // Then, no exception
    }

    @Test
    public void shouldReturnEmptyResultSetWhenNoPapersOrAuthorsMatchSearchTerm() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueString = getRandomString();
        library.update(asList(aSearchIndexPaper().withTitle("not the paper you are looking for").build()));

        // When
        BaseSearchResult[] searchResults = library.searchForItemsMatching(uniqueString, 0, 1).getResults();

        // Then
        assertThat(searchResults, is(emptyArray()));
    }

    @Test
    public void shouldFindSinglePaperContainingJustOneOfTwoSearchTerms() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueKnownStringInPaperTitle = getRandomString();
        String highlightedUniqueKnownStringInPaperTitle = String.format("<em>%s</em>", uniqueKnownStringInPaperTitle);

        SearchIndexPaper thePaper = aSearchIndexPaper()
                .withTitle(String.format("First SearchIndexPaper Title %s", uniqueKnownStringInPaperTitle))
                .build();

        // When
        library.update(asList(thePaper));

        final String searchTerms = String.format("%s %s", uniqueKnownStringInPaperTitle, getRandomString());

        // Then
        assertThat(
                () -> library.searchForItemsMatching(searchTerms, 0, 1).getResults(),
                eventuallySatisfies(allOf(
                        is(arrayWithSize(1)),
                        hasItemInArray(is(searchResultWith(matchesAllOf(id(thePaper.getId()), paperTitle(thePaper.getTitle().replace(uniqueKnownStringInPaperTitle, highlightedUniqueKnownStringInPaperTitle))))))
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldFindPapersEachContainingJustOneOfTwoDistinctSearchTerms() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        // When

        String randomString1 = getRandomString();
        String randomString2 = getRandomString();
        String[] searchStrings = {randomString1, randomString2};
        String[] expectedHighlightedSearchStrings = {String.format("<em>%s</em>", randomString1), String.format("<em>%s</em>", randomString2)};

        SearchIndexPaper firstPaper = aSearchIndexPaper()
                .withTitle(String.format("SearchIndexPaper Title %s", searchStrings[0]))
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withTitle(String.format("SearchIndexPaper Title %s", searchStrings[1]))
                .build();
        library.update(asList(firstPaper, secondPaper));

        String searchTerms = String.format("%s %s", searchStrings[0], searchStrings[1]);

        // Then
        assertThat(
                () -> library.searchForItemsMatching(searchTerms, 0, 50).getResults(),
                eventuallySatisfies(allOf(
                        is(arrayWithSize(2)),
                        hasItemInArray(is(searchResultWith(matchesAllOf(id(firstPaper.getId()), paperTitle(firstPaper.getTitle().replace(searchStrings[0], expectedHighlightedSearchStrings[0])))))),
                        hasItemInArray(is(searchResultWith(matchesAllOf(id(secondPaper.getId()), paperTitle(secondPaper.getTitle().replace(searchStrings[1], expectedHighlightedSearchStrings[1]))))))
                )).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );
    }

    @Test
    public void shouldRetrievePapersWrittenBySpecifiedAuthors() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                1
        );

        String uniqueString = getRandomString();

        SearchIndexPaper firstPaper = aSearchIndexPaper()
                .withId("1")
                .withTitle(String.format("Paper %s 1", uniqueString))
                .withKeywords("any string")
                .withAuthors(aSearchIndexPaperAuthor().withId("1").withName("Tom").build())
                .build();

        SearchIndexPaper secondPaper = aSearchIndexPaper()
                .withId("2")
                .withTitle(String.format("Paper %s 2", uniqueString))
                .withAuthors(aSearchIndexPaperAuthor().withId("2").withName("Jim").build())
                .build();

        library.update(asList(firstPaper, secondPaper));

        assertThat(() -> library.searchForItemsMatching(uniqueString, 0, 50).getResults(),
                eventuallySatisfies(arrayContainingInAnyOrder(
                        searchResultWith(id("1")),
                        searchResultWith(id("2"))
                )).within(3, SECONDS, checkingEvery(100, MILLISECONDS)));

        List<AuthorUpdate.Author> authors = asList(
                anAuthor().withId("1").build(),
                anAuthor().withId("2").build()
        );

        // When
        Stream<SearchIndexPaper> streamOfSearchIndexPapers = library.getPapersWrittenBy(authors);

        // Then
        List<SearchIndexPaper> searchIndexPapers = streamOfSearchIndexPapers.collect(Collectors.toList());

        assertThat(searchIndexPapers, hasSize(2));
        assertThat(searchIndexPapers, hasItems(
                searchIndexPaperWith(
                        paperId("1"),
                        SearchIndexPaperTitleMatcher.paperTitle(firstPaper.getTitle()),
                        SearchIndexPaperKeywordsMatcher.paperKeywords("any string"),
                        authorsInOrder(
                                authorWith(
                                        SearchIndexPaperAuthorIdMatcher.authorId("1"),
                                        SearchIndexPaperAuthorNameMatcher.authorName("Tom"))
                        )),
                searchIndexPaperWith(
                        paperId("2"),
                        SearchIndexPaperTitleMatcher.paperTitle(secondPaper.getTitle()),
                        SearchIndexPaperKeywordsMatcher.paperKeywords(null),
                        authorsInOrder(
                                authorWith(
                                        SearchIndexPaperAuthorIdMatcher.authorId("2"),
                                        SearchIndexPaperAuthorNameMatcher.authorName("Jim"))
                        ))
        ));
    }

    @Test
    public void shouldLimitSearchResultSetContainingSearchTerm() {
        // Given
        ElasticsearchLibrary library = new ElasticsearchLibrary(
                ELASTICSEARCH_PAPERS_INDEX_NAME,
                ELASTICSEARCH_AUTHORS_INDEX_NAME,
                elasticsearchCluster().nodeHostname(),
                elasticsearchCluster().nodePort(),
                10
        );

        String uniqueString = getRandomString();

        SearchIndexPaper firstPaperWithSearchTermInTitle = aSearchIndexPaper()
                .withTitle(String.format("%s 1", uniqueString))
                .withAuthors(aSearchIndexPaperAuthor().withId("2").withName("Matt"), aSearchIndexPaperAuthor().withId("3").withName("Erik"))
                .build();

        SearchIndexPaper secondPaperWithSearchTermInTitle = aSearchIndexPaper()
                .withTitle(String.format("%s 2", uniqueString))
                .withAuthors(aSearchIndexPaperAuthor().withId("5").withName("Trupti"))
                .build();

        library.update(asList(firstPaperWithSearchTermInTitle, secondPaperWithSearchTermInTitle));

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 0, 1).getTotalNumberOfResults(),
                // Then
                eventuallySatisfies(is(equalTo(2))).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 0, 1).getResults(),
                // Then
                eventuallySatisfies(arrayWithSize(1)).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 1, 1).getTotalNumberOfResults(),
                // Then
                eventuallySatisfies(is(equalTo(2))).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );

        assertThat(
                // When
                () -> library.searchForItemsMatching(uniqueString, 1, 1).getResults(),
                // Then
                eventuallySatisfies(arrayWithSize(1)).within(10, SECONDS, checkingEvery(100, MILLISECONDS))
        );

        // When
        String firstPagePaperTitle = ((PaperSearchResult)library.searchForItemsMatching(uniqueString, 0, 1).getResults()[0]).getTitle();
        String secondPagePaperTitle = ((PaperSearchResult)library.searchForItemsMatching(uniqueString, 1, 1).getResults()[0]).getTitle();

        // Then
        assertThat(firstPagePaperTitle, is(Matchers.not(Matchers.equalTo(secondPagePaperTitle))));

        String expectedHighlightedPaperTitleOne = String.format("<em>%s</em> 1", uniqueString);
        String expectedHighlightedPaperTitleTwo = String.format("<em>%s</em> 2", uniqueString);

        List<String> titleNames = Arrays.asList(firstPagePaperTitle, secondPagePaperTitle);
        MatcherAssert.assertThat(titleNames, containsInAnyOrder(expectedHighlightedPaperTitleOne, expectedHighlightedPaperTitleTwo));
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @SuppressWarnings("unchecked")
    private static <TInput, TOutput> List<TOutput> listOf(Function<TInput, TOutput> mapItem, TInput... array) {
        return Arrays.stream(array).map(mapItem).collect(Collectors.toList());
    }


}
