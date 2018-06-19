package com.ssrn.frontend.service_test;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.frontend.website.fake_search_service.FakeSearchService;
import com.ssrn.test.support.ssrn.website.pagemodel.*;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static com.ssrn.frontend.website.fake_search_service.IndexedAuthorBuilder.anIndexedAuthor;
import static com.ssrn.frontend.website.fake_search_service.IndexedPaperAuthorBuilder.anIndexedPaperAuthor;
import static com.ssrn.frontend.website.fake_search_service.IndexedPaperBuilder.anIndexedPaper;
import static com.ssrn.test.support.golden_data.FakeSsrnUsers.JAMES_BROWN;
import static com.ssrn.test.support.golden_data.FakeSsrnUsers.TIM_DUNCAN;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultAuthorMatcher.anAuthor;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultAuthorWithNameMatcher.withName;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultMatcher.searchResultWith;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithAuthorMatcher.anAuthorSearchResultWith;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithAuthorNamesMatcher.authorNamesInOrder;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithKeywordsMatcher.keywords;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithNoAuthorsMatcher.noAuthors;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithNoKeywordsMatcher.noKeywords;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithTitleMatcher.title;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class FrontendServiceTest extends SsrnFakeOldPlatformTest {

    private FakeSearchService fakeSearchService;
    private NewSsrnWebsite newSsrnWebsite;

    private FakeOldPlatform fakeOldPlatform;

    @Before
    public void before() {
        fakeSearchService = new FakeSearchService();
        fakeSearchService.reset();

        newSsrnWebsite = new NewSsrnWebsite(ssrnWebsite(), 3);
        fakeOldPlatform = new FakeOldPlatform();
        fakeOldPlatform.resetOverrides();

        waitUntil(() -> newSsrnWebsite.isAvailable())
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(15, SECONDS);
    }

    @Test
    public void shouldDisplayUserMenuWhenUserIsLoggedIn() {
        // Given
        SearchPage.Visit searchPageVisit = browser().visit(newSsrnWebsite().searchPage());
        assertThat(searchPageVisit.loggedInStatus(), is("false"));

        // When
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        // Then
        searchPageVisit = browser().visit(newSsrnWebsite().searchPage());
        assertThat(searchPageVisit.loggedInStatus(), is("true"));
    }

    @Test
    public void searchingReturnsMutiplePages() {
        // Given

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String firstAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("First Статья Title")
                .getAbstractId();

        String secondAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("Second Статья Title")
                .getAbstractId();

        String thirdAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("Third Статья Title")
                .getAbstractId();

        String fourthAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("Fourth Статья Title")
                .getAbstractId();

        String fifthAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("Fifth Статья Title")
                .getAbstractId();

        fakeSearchService.has(
                anIndexedPaper().withId(firstAbstractId).withTitle("First Статья Title").withAuthors(anIndexedPaperAuthor().withId(ssrnWebsite().accountId())),
                anIndexedPaper().withId(secondAbstractId).withTitle("Second Статья Title").withAuthors(anIndexedPaperAuthor().withId(ssrnWebsite().accountId())),
                anIndexedPaper().withId(thirdAbstractId).withTitle("Third Статья Title").withAuthors(anIndexedPaperAuthor().withId(ssrnWebsite().accountId())),
                anIndexedPaper().withId(fourthAbstractId).withTitle("Fourth Статья Title").withAuthors(anIndexedPaperAuthor().withId(ssrnWebsite().accountId())),
                anIndexedPaper().withId(fifthAbstractId).withTitle("Fifth Статья Title").withAuthors(anIndexedPaperAuthor().withId(ssrnWebsite().accountId()))
        );

        // When
        SearchPage.Visit searchPageVisit = browser().visit(newSsrnWebsite().searchPage());

        // When
        searchPageVisit.searchForTitle("статья");

        // Then
        assertThat(searchPageVisit.nextLink(), notNullValue());
        assertThat(searchPageVisit.prevLink(), nullValue());

        // When
        searchPageVisit.clickNextLink();

        // Then
        assertThat(searchPageVisit.prevLink(), notNullValue());
        assertThat(searchPageVisit.nextLink(), notNullValue());

        // When
        searchPageVisit.clickNextLink();

        // Then
        assertThat(searchPageVisit.nextLink(), nullValue());
        assertThat(searchPageVisit.prevLink(), notNullValue());

        // When
        searchPageVisit.clickPrevLink();

        // Then
        assertThat(searchPageVisit.prevLink(), notNullValue());
        assertThat(searchPageVisit.nextLink(), notNullValue());

        // When
        searchPageVisit.clickPrevLink();

        // Then
        assertThat(searchPageVisit.nextLink(), notNullValue());
        assertThat(searchPageVisit.prevLink(), nullValue());
    }

    @Test
    public void searchingForPapersByTitle() {
        // Given

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String firstAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("First Статья Title")
                .addAuthorToPaper(TIM_DUNCAN.getEmailAddress(), TIM_DUNCAN.getId())
                .addAuthorToPaper(JAMES_BROWN.getEmailAddress(), JAMES_BROWN.getId())
                .getAbstractId();

        String secondAbstractId = browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("Second Статья Title")
                .getAbstractId();

        fakeSearchService.has(
                anIndexedPaper().withId(firstAbstractId).withTitle("First Статья Title")
                        .withKeywords("Elections, Democracy, Nationalism")
                        .withAuthors(
                                anIndexedPaperAuthor().withId(TIM_DUNCAN.getId()).withName(TIM_DUNCAN.getPublicDisplayName()),
                                anIndexedPaperAuthor().withId(JAMES_BROWN.getId()).withName(JAMES_BROWN.getPublicDisplayName())
                        ),
                anIndexedPaper().withId(secondAbstractId).withTitle("Second Статья Title")
                        .withAuthors(
                                anIndexedPaperAuthor().withId("103").withNoName()
                        ),
                anIndexedPaper().withTitle("Third Article Title")
                        .withAuthors(
                                anIndexedPaperAuthor().withId("104").withNoName()
                        )
        );

        // When
        SearchPage.Visit searchPageVisit = browser().visit(newSsrnWebsite().searchPage());

        // Then
        assertThat(searchPageVisit.searchBoxText(), is(emptyString()));

        // When
        searchPageVisit.searchForTitle("non-existent");

        // Then
        assertThat(searchPageVisit.searchResults(), is(empty()));

        // When
        searchPageVisit.searchForTitle("статья");

        // Then
        assertThat(searchPageVisit.numberOfSearchResults(), is(equalTo("2")));
        assertThat(searchPageVisit.searchResults(), hasSize(2));
        assertThat(searchPageVisit.searchResults(), hasItems(
                searchResultWith(allOf(
                        title("First Статья Title"),
                        authorNamesInOrder(
                                TIM_DUNCAN.getPublicDisplayName(),
                                JAMES_BROWN.getPublicDisplayName()
                        ),
                        keywords("Elections, Democracy, Nationalism"))),
                searchResultWith(allOf(title("Second Статья Title"), noAuthors(), noKeywords()))
        ));

        // When
        SearchResult firstSearchResult = searchPageVisit.searchResults().get(0);
        String firstSearchResultTitle = firstSearchResult.title();
        ArticlePage.Visit anArticlePageVisit = firstSearchResult.click();

        // Then
        assertThat(anArticlePageVisit.title(), is(equalTo(firstSearchResultTitle)));

        // When
        browser().clickBackButton();
        SearchResult secondSearchResult = searchPageVisit.searchResults().get(1);
        String secondSearchResultTitle = secondSearchResult.title();
        ArticlePage.Visit anotherArticlePageVisit = secondSearchResult.click();

        // Then
        assertThat(anotherArticlePageVisit.title(), is(equalTo(secondSearchResultTitle)));

        // When
        browser().clickBackButton();
        AuthorProfilePage.Visit authorProfilePage = firstSearchResult.clickPaperAuthor(0);

        // Then
        assertThat(authorProfilePage.authorName(), is(equalTo(TIM_DUNCAN.getPublicDisplayName())));

        // When
        browser().clickBackButton();
        authorProfilePage = firstSearchResult.clickPaperAuthor(1);

        // Then
        assertThat(authorProfilePage.authorName(), is(equalTo(JAMES_BROWN.getPublicDisplayName())));
    }

    @Test
    public void searchingForPapersAndAuthorsByAuthors() {
        // Given
        int uniqueNumber = new Random().nextInt(9999);
        String firstName = String.format("Test %d", uniqueNumber);
        String lastName = "User 1";
        String uniqueAuthorName = String.format("%s %s", firstName, lastName);
        Integer uniqueAuthorId = fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(uniqueAuthorName);

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        fakeSearchService.has(
                anIndexedPaper().withId(UUID.randomUUID().toString()).withTitle("First Статья Title")
                        .withAuthors(
                                anIndexedPaperAuthor().withId(String.valueOf(uniqueAuthorId)).withName(uniqueAuthorName)
                        )
        );

        fakeSearchService.has(anIndexedAuthor().withId(String.valueOf(uniqueAuthorId)).withName(uniqueAuthorName));

        // When
        SearchPage.Visit searchPageVisit = browser().visit(newSsrnWebsite().searchPage());
        searchPageVisit.searchForTitle(uniqueAuthorName.toLowerCase());

        // Then
        assertThat(searchPageVisit.numberOfSearchResults(), is(equalTo("2")));

        assertThat(searchPageVisit.searchResults(), hasSize(2));

        assertThat(searchPageVisit.searchResults(), hasItem(
                searchResultWith(allOf(
                        title("First Статья Title"),
                        authorNamesInOrder(
                                uniqueAuthorName
                        )))
        ));

        assertThat(searchPageVisit.searchResults(), hasItem(
                anAuthorSearchResultWith(anAuthor(withName(uniqueAuthorName)))
        ));

        // When
        AuthorProfilePage.Visit authorProfilePage = searchPageVisit.searchResults().get(1).clickAuthor();

        // Then
        assertThat(authorProfilePage.authorName(), is(equalTo(uniqueAuthorName)));
    }

    @Test
    public void searchingForPapersByKeywords() {
        // Given
        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        fakeSearchService.has(
                anIndexedPaper().withTitle("First Статья Title")
                        .withKeywords("Elections, Democracy, Nationalism")
                        .withAuthors(
                                anIndexedPaperAuthor().withId(TIM_DUNCAN.getId()).withName(TIM_DUNCAN.getPublicDisplayName()),
                                anIndexedPaperAuthor().withId(JAMES_BROWN.getId()).withName(JAMES_BROWN.getPublicDisplayName())
                        ),
                anIndexedPaper().withTitle("Second Статья Title")
                        .withKeywords("Programming, Nationalism")
                        .withAuthors(
                                anIndexedPaperAuthor().withId(TIM_DUNCAN.getId()).withName(TIM_DUNCAN.getPublicDisplayName()),
                                anIndexedPaperAuthor().withId(JAMES_BROWN.getId()).withName(JAMES_BROWN.getPublicDisplayName())
                        ),
                anIndexedPaper().withTitle("Third Article Title").withAuthors(
                        anIndexedPaperAuthor().withId(TIM_DUNCAN.getId()).withName(TIM_DUNCAN.getPublicDisplayName()),
                        anIndexedPaperAuthor().withId(JAMES_BROWN.getId()).withName(JAMES_BROWN.getPublicDisplayName())
                )
        );

        // When
        SearchPage.Visit searchPageVisit = browser().visit(newSsrnWebsite().searchPage());
        searchPageVisit.searchForTitle("nationalism");

        // Then
        assertThat(searchPageVisit.searchResults(), hasSize(2));
        assertThat(searchPageVisit.searchResults(), hasItems(
                searchResultWith(allOf(
                        title("First Статья Title"),
                        keywords("Elections, Democracy, Nationalism"))),
                searchResultWith(allOf(
                        title("Second Статья Title"),
                        keywords("Programming, Nationalism")))
        ));
    }

    @Test
    public void shouldPreventAccessToSearchPageIfNoCredentialsAreGiven() {
        // Given
        Client client = ClientBuilder.newClient();

        // When
        Response response = client.target(newSsrnWebsite().absoluteUrlTo("/fastsearch")).request().get();

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));
    }

    @Test
    public void shouldAllowAccessToSearchPageWhenCorrectCredentialsAreGiven() {
        // Given
        Client client = ClientBuilder.newClient();

        // When
        Response response = client.target(newSsrnWebsite().absoluteUrlTo("/fastsearch"))
                .request()
                .header("Authorization",
                        ssrnBasicAuthenticationHeader(newSsrnWebsite().getUsername(), newSsrnWebsite().getPassword()))
                .get();

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
    }

    @Test
    public void shouldProvideCacheControlForKnownStaticResources() {
        // Given
        String[] knownStaticResources = new String[]{
                "/images/ssrn-logo.png",
                "/images/logo_dark.svg",
                "/js/menu.js",
                "/fonts/nexus_sans/bold/NexusSansWebPro-Bold.ttf",
                "/fonts/nexus_sans/bold/NexusSansWebPro-Bold.eot",
                "/fonts/nexus_sans/bold/NexusSansWebPro-Bold.woff",
                "/css/search.css"};

        Client client = ClientBuilder.newClient();

        Arrays.asList(knownStaticResources).forEach(url -> {
            // When
            Response response = client.target(newSsrnWebsite().absoluteUrlTo(url)).request().get();

            // Then
            assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
            assertThat(response.getHeaderString("Cache-Control"), is(equalTo("max-age=7200")));
        });

        Response response = client.target(newSsrnWebsite().absoluteUrlTo("/fastsearch"))
                .request()
                .header("Authorization",
                        ssrnBasicAuthenticationHeader(newSsrnWebsite().getUsername(), newSsrnWebsite().getPassword()))
                .get();

        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(response.getHeaderString("Cache-Control"), is(equalTo("no-cache")));
    }

    @Test
    public void shouldNotAllowAccessToSearchPageWhenIncorrectCredentialsAreGiven() {
        // Given
        Client client = ClientBuilder.newClient();

        // When
        Response response = client.target(newSsrnWebsite().absoluteUrlTo("/fastsearch"))
                .request()
                .header("Authorization",
                        ssrnBasicAuthenticationHeader(
                                String.format("%s-X", newSsrnWebsite().getUsername()),
                                String.format("%s-X", newSsrnWebsite().getPassword())))
                .get();

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.UNAUTHORIZED)));

    }

    private String ssrnBasicAuthenticationHeader(String username, String password) {
        return base64EncodedBasicAuthorizationHeader(username, password);
    }

    private NewSsrnWebsite newSsrnWebsite() {
        return newSsrnWebsite;
    }

}
