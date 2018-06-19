package com.ssrn.frontend.website;

import com.ssrn.frontend.website.fake_search_service.FakeSearchService;
import com.ssrn.frontend.website.search.*;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ssrn.frontend.website.fake_search_service.IndexedAuthorBuilder.anIndexedAuthor;
import static com.ssrn.frontend.website.fake_search_service.IndexedPaperAuthorBuilder.anIndexedPaperAuthor;
import static com.ssrn.frontend.website.fake_search_service.IndexedPaperBuilder.anIndexedPaper;
import static com.ssrn.frontend.website.fake_search_service.OverriddenResponse.respondWithStatus;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;

public class SearchApiSearchEngineTest {

    private static final String ANY_QUERY = "paper";
    private static final int DEFAULT_PAGE_SIZE = 50;
    private FakeSearchService fakeSearchService;

    @Before
    public void before() {
        fakeSearchService = new FakeSearchService();
        fakeSearchService.reset();
    }

    @Test
    public void shouldProvideCountOfTotalNumberOfSearchResultsRetrievedFromSearchApi() {
        // Given
        String aUniquePaperTitle = UUID.randomUUID().toString();
        fakeSearchService.has(
                anIndexedPaper()
                        .withTitle(String.format("Paper 1 %s", aUniquePaperTitle))
                        .withAuthors(
                                anIndexedPaperAuthor().withId("103").withName("Jackie Paper")),
                anIndexedPaper()
                        .withTitle(String.format("Paper 2 %s", aUniquePaperTitle))
                        .withAuthors(
                                anIndexedPaperAuthor().withId("103").withName("Jackie Paper"))
        );

        SearchApiSearchEngine searchApiSearchEngine = new SearchApiSearchEngine(fakeSearchService.getBaseUrl(), createJerseyClient(), 1);

        // When
        SearchResults searchResultsPageOne = searchApiSearchEngine.findItemsMatching(aUniquePaperTitle, 0);

        // Then
        assertThat(searchResultsPageOne.getTotalNumberOfResults(), is(2));
        assertThat(searchResultsPageOne.getSearchResults().length, is(1));

        // When
        SearchResults searchResultsPageTwo = searchApiSearchEngine.findItemsMatching(aUniquePaperTitle, 1);

        // Then
        assertThat(searchResultsPageTwo.getTotalNumberOfResults(), is(2));
        assertThat(searchResultsPageTwo.getSearchResults().length, is(1));

        String searchResultOneTitle = ((PaperSearchResult) searchResultsPageOne.getSearchResults()[0]).getTitle();
        String searchResultTwoTitle = ((PaperSearchResult) searchResultsPageTwo.getSearchResults()[0]).getTitle();
        assertThat(searchResultOneTitle, is(not(equals(searchResultTwoTitle))));

        String expectedHighlightedPaperTitleOne = String.format("Paper 1 <em>%s</em>", aUniquePaperTitle);
        String expectedHighlightedPaperTitleTwo = String.format("Paper 2 <em>%s</em>", aUniquePaperTitle);

        List<String> titleNames = Arrays.asList(searchResultOneTitle, searchResultTwoTitle);
        assertThat(titleNames, containsInAnyOrder(expectedHighlightedPaperTitleOne, expectedHighlightedPaperTitleTwo));
    }

    @Test
    public void shouldReturnEmptySearchResultsWhenSearchApiDoesNotRespondWithOKStatus() {
        // Given
        fakeSearchService.nextResponse(respondWithStatus(BAD_REQUEST));

        SearchApiSearchEngine searchApiSearchEngine = new SearchApiSearchEngine(fakeSearchService.getBaseUrl(), createJerseyClient(), 1);

        // When
        BaseSearchResult[] searchResults = searchApiSearchEngine.findItemsMatching(ANY_QUERY, 0).getSearchResults();

        // Then
        assertThat(searchResults, arrayWithSize(0));
    }

    @Test
    public void shouldProvideSearchResultsRetrievedFromSearchApi() {
        // Given
        String firstPaperId = UUID.randomUUID().toString();
        String secondPaperId = UUID.randomUUID().toString();
        String thirdPaperId = UUID.randomUUID().toString();
        String anAuthorId = UUID.randomUUID().toString();

        fakeSearchService.has(
                anIndexedPaper()
                        .withId(firstPaperId)
                        .withTitle("A Paper Title")
                        .withAuthors(
                                anIndexedPaperAuthor().withId("102").withName("Indexed Paper Author One Zero Two"),
                                anIndexedPaperAuthor().withId("101").withName("Indexed Paper Author One Zero One")
                        )
                        .withKeywords("first paper word"),
                anIndexedPaper().withId(secondPaperId).withTitle("an article title"),
                anIndexedPaper().withId(thirdPaperId).withTitle("another title").withAuthors(anIndexedPaperAuthor().withId("103").withName("Jackie Paper"))
        );

        fakeSearchService.has(anIndexedAuthor().withId(anAuthorId).withName("Paper Mario"));

        SearchApiSearchEngine searchApiSearchEngine = new SearchApiSearchEngine(fakeSearchService.getBaseUrl(), createJerseyClient(), 50);

        // When
        BaseSearchResult[] searchResults = searchApiSearchEngine.findItemsMatching("Paper", 0).getSearchResults();

        // Then
        assertThat(searchResults, arrayWithSize(3));
        assertThat(searchResults, hasItemInArray(samePropertyValuesAs(
                new PaperSearchResult(
                        firstPaperId,
                        "A <em>Paper</em> Title",
                        "first <em>paper</em> word",
                        new SearchResultAuthor[]{
                                new SearchResultAuthor("102", "Indexed <em>Paper</em> Author One Zero Two"),
                                new SearchResultAuthor("101", "Indexed <em>Paper</em> Author One Zero One")
                        }
                ))));

        assertThat(searchResults, hasItemInArray(samePropertyValuesAs(new PaperSearchResult(thirdPaperId, "another title",
                null, new SearchResultAuthor[]{
                new SearchResultAuthor("103", "Jackie <em>Paper</em>")
        }))));

        assertThat(searchResults, hasItemInArray(samePropertyValuesAs(new AuthorSearchResult(anAuthorId, "<em>Paper</em> Mario"))));

        assertThat(searchResults, not(hasItemInArray(samePropertyValuesAs(new PaperSearchResult(secondPaperId, "an article title",
                null, new SearchResultAuthor[0])))));
    }

    @Test
    public void shouldReturnEmptySearchResultWhenSearchApiNotAvailable() {
        // Given
        String nonExistentUrl = "http://localhost:0000";
        SearchApiSearchEngine searchApiSearchEngine = new SearchApiSearchEngine(nonExistentUrl, createJerseyClient(), DEFAULT_PAGE_SIZE);

        // When
        BaseSearchResult[] papersMatching = searchApiSearchEngine.findItemsMatching(ANY_QUERY, 0).getSearchResults();

        // Then
        assertThat(papersMatching, arrayWithSize(0));
    }

    private static Client createJerseyClient() {
        return ClientBuilder.newClient()
                .property(ClientProperties.READ_TIMEOUT, 3000)
                .property(ClientProperties.CONNECT_TIMEOUT, 3000);
    }

}
