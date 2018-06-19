package com.ssrn.frontend.website.search_service_contract_test;

import com.jayway.jsonpath.JsonPath;
import com.ssrn.frontend.website.fake_search_service.FakeSearchService;
import com.ssrn.frontend.website.fake_search_service.IndexedAuthorBuilder;
import com.ssrn.frontend.website.fake_search_service.IndexedPaperAuthorBuilder;
import com.ssrn.frontend.website.fake_search_service.IndexedPaperBuilder;
import com.ssrn.test.support.golden_data.RealSsrnUsers;
import com.ssrn.test.support.golden_data.SsrnPaper;
import com.ssrn.test.support.golden_data.SsrnPapers;
import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.ssrn.frontend.website.fake_search_service.IndexedAuthorBuilder.anIndexedAuthor;
import static com.ssrn.frontend.website.fake_search_service.IndexedPaperAuthorBuilder.anIndexedPaperAuthor;
import static com.ssrn.frontend.website.fake_search_service.IndexedPaperBuilder.anIndexedPaper;
import static com.ssrn.test.support.golden_data.FakeSsrnUsers.*;
import static com.ssrn.test.support.golden_data.SsrnPapers.*;
import static com.ssrn.test.support.http.HttpClient.*;
import static com.ssrn.test.support.http.HttpResponseBodyMatcher.bodySatisfying;
import static com.ssrn.test.support.http.HttpResponseMatcher.anHttpResponseWith;
import static com.ssrn.test.support.http.HttpStatusCodeMatcher.statusCode;
import static com.ssrn.test.support.http.InspectableResponse.asInspectableResponse;
import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SearchServiceContract {

    private static final boolean TESTING_AGAINST_FAKE = System.getenv("SEARCH_SERVICE_BASE_URL") == null;
    private static String SEARCH_SERVICE_BASE_URL = TESTING_AGAINST_FAKE ?
            FakeSearchService.BASE_URL :
            System.getenv("SEARCH_SERVICE_BASE_URL");
    private static FakeSearchService fakeSearchService;

    @BeforeClass
    public static void beforeClass() {
        HttpClient httpClient = createHttpClient();

        if (TESTING_AGAINST_FAKE) {
            fakeSearchService = new FakeSearchService();
            fakeSearchService.reset();
            fakeSearchService.has(
                    SsrnPapers.all()
                            .filter(ssrnPaper -> !isNonSearchablePaper(ssrnPaper))
                            .map(paper -> anIndexedPaper()
                                    .withId(paper.getId())
                                    .withTitle(paper.getTitle())
                                    .withAuthors(Arrays.stream(paper.getAuthors())
                                            .map(author -> anIndexedPaperAuthor().withId(author.getId()).withName(author.getPublicDisplayName()))
                                            .toArray(IndexedPaperAuthorBuilder[]::new)
                                    )
                                    .withKeywords(paper.getKeywords()))
                            .toArray(IndexedPaperBuilder[]::new)
            );

            fakeSearchService.has(
                    SsrnPapers.all()
                            .filter(ssrnPaper -> !(isNonSearchablePaper(ssrnPaper)))
                            .flatMap(paper -> Stream.of(paper.getAuthors()))
                            .distinct()
                            .map(author -> anIndexedAuthor().withId(author.getId()).withName(author.getPublicDisplayName()))
                            .toArray(IndexedAuthorBuilder[]::new)
            );

        } else {
            waitUntil(() -> httpClient.get(SEARCH_SERVICE_BASE_URL, "/healthcheck").getStatus() == OK.getStatusCode())
                    .checkingEvery(100, MILLISECONDS)
                    .forNoMoreThan(90, SECONDS);
        }
    }

    @Test
    public void expectSearchApiToProvidePapersWithAuthorIdsAndNamesThatMatchedTitleSearchQuery() {
        // Given
        HttpClient httpClient = createHttpClient();

        String searchString = "Market";
        String expectedHighlightedSearchString = String.format("<em>%s</em>", searchString);

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 0),
                                queryParameter("size", 50)
                        )
                )),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s')]", PAPER_52.getId(), PAPER_52.getTitle().replace(searchString, expectedHighlightedSearchString), RealSsrnUsers.USER_54.getId(), RealSsrnUsers.USER_54.getPublicDisplayName(), RealSsrnUsers.USER_1341.getId(), RealSsrnUsers.USER_1341.getPublicDisplayName())),
                                                hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s' && @.authors[2].id == '%s' && @.authors[2].name == '%s')]", PAPER_20005.getId(), PAPER_20005.getTitle().replace(searchString, expectedHighlightedSearchString), BILLY_BOB.getId(), BILLY_BOB.getPublicDisplayName(), LUCY_JONES.getId(), LUCY_JONES.getPublicDisplayName(), JOHN_DOE.getId(), JOHN_DOE.getPublicDisplayName())),
                                                not(hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s')]", PAPER_42.getId(), PAPER_42.getTitle().replace(searchString, expectedHighlightedSearchString), RealSsrnUsers.USER_35974.getId(), RealSsrnUsers.USER_35974.getPublicDisplayName(), RealSsrnUsers.USER_35976.getId(), RealSsrnUsers.USER_35976.getPublicDisplayName()))),
                                                not(hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s')]", PAPER_45.getId())))
                                        )))))
                ).within(180, SECONDS, checkingEvery(1, SECONDS))
        );
    }

    @Test
    public void expectSearchApiToReturnTotalNumberOfResults() {
        // Given
        HttpClient httpClient = createHttpClient();


        String searchString = "Market";

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 0),
                                queryParameter("size", 50)
                        )
                )),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath("$['totalNumberOfResults']", is(3))
                                        )))))
                ).within(10, SECONDS, checkingEvery(1, SECONDS))
        );
    }

    @Test
    public void expectSearchApiToReturnMultiplePagesOfResults() {
        // Given
        HttpClient httpClient = createHttpClient();

        String searchString = "Market";

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 0),
                                queryParameter("size", 1)
                        )

                )),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath("$['totalNumberOfResults']", is(3)),
                                                hasJsonPath("$.results.length()", is(equalTo(1)))

                                        )))))
                ).within(10, SECONDS, checkingEvery(1, SECONDS))
        );

        Response response = httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", searchString),
                        queryParameter("from", 0),
                        queryParameter("size", 1)
                ));

        String firstPagePaperTitle = JsonPath.read(response.readEntity(String.class), "$.results[0].title");

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 1),
                                queryParameter("size", 1)
                        )

                )),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath("$['totalNumberOfResults']", is(3)),
                                                hasJsonPath("$.results.length()", is(equalTo(1)))

                                        )))))
                ).within(10, SECONDS, checkingEvery(1, SECONDS))
        );

        response = httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", searchString),
                        queryParameter("from", 1),
                        queryParameter("size", 1)
                ));

        String secondPagePaperTitle = JsonPath.read(response.readEntity(String.class), "$.results[0].title");

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 2),
                                queryParameter("size", 1)
                        )

                )),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath("$['totalNumberOfResults']", is(3)),
                                                hasJsonPath("$.results.length()", is(equalTo(1)))

                                        )))))
                ).within(10, SECONDS, checkingEvery(1, SECONDS))
        );

        response = httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                queryParameters(
                        queryParameter("query", searchString),
                        queryParameter("from", 2),
                        queryParameter("size", 1)
                ));

        String thirdPagePaperTitle = JsonPath.read(response.readEntity(String.class), "$.results[0].title");

        assertThat(firstPagePaperTitle, is(not(equalTo(secondPagePaperTitle))));
        assertThat(secondPagePaperTitle, is(not(equalTo(thirdPagePaperTitle))));

        String expectedHighlightedPaperTitleOne = "Messages from <em>Market</em> to Management: The Case of Ipos";
        String expectedHighlightedPaperTitleTwo = "The Cost of Equity and Exchange Listing: Evidence from the French Stock <em>Market</em>";
        String expectedHighlightedPaperTitleThree = "a paper about the <em>Market</em>";

        List<String> titleNames = Arrays.asList(firstPagePaperTitle, secondPagePaperTitle, thirdPagePaperTitle);
        MatcherAssert.assertThat(titleNames, containsInAnyOrder(expectedHighlightedPaperTitleOne, expectedHighlightedPaperTitleTwo, expectedHighlightedPaperTitleThree));

    }

    @Test
    public void expectSearchApiToProvidePapersWithAuthorIdsAndNamesAndKeywordsThatMatchedTitleSearchQuery() {
        // Given
        HttpClient httpClient = createHttpClient();

        String searchString = "Cyberspace";
        String expectedHighlightedSearchString = String.format("<em>%s</em>", searchString);

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 0),
                                queryParameter("size", 50)
                        ))),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s' && @.keywords == '%s')]",
                                                        PAPER_535.getId(), PAPER_535.getTitle().replace(searchString, expectedHighlightedSearchString), RealSsrnUsers.USER_536.getId(), RealSsrnUsers.USER_536.getPublicDisplayName(), RealSsrnUsers.USER_537.getId(), RealSsrnUsers.USER_537.getPublicDisplayName(), PAPER_535.getKeywords().replace(searchString, expectedHighlightedSearchString)))
                                        )))))
                ).within(180, SECONDS, checkingEvery(1, SECONDS))
        );
    }

    @Test
    public void expectSearchApiToProvidePapersWithAuthorIdsAndNamesAndKeywordsThatMatchedKeywordsSearchQuery() {
        // Given
        HttpClient httpClient = createHttpClient();

        String searchString = "Internet";
        String expectedHighlightedSearchString = String.format("<em>%s</em>", searchString);

        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 0),
                                queryParameter("size", 50)
                        ))),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s' && @.keywords == '%s')]", PAPER_535.getId(), PAPER_535.getTitle().replace(searchString, expectedHighlightedSearchString), RealSsrnUsers.USER_536.getId(), RealSsrnUsers.USER_536.getPublicDisplayName(), RealSsrnUsers.USER_537.getId(), RealSsrnUsers.USER_537.getPublicDisplayName(), PAPER_535.getKeywords().replace(searchString, expectedHighlightedSearchString)))
                                        )))))
                ).within(180, SECONDS, checkingEvery(1, SECONDS))
        );
    }

    @Test
    public void expectSearchApiToProvidePapersWithAuthorIdsAndNamesThatMatchedAuthorNameSearchQuery() {
        // Given
        HttpClient httpClient = createHttpClient();

        String searchString = RealSsrnUsers.USER_54.getPublicDisplayName();
        String expectedHighlightedSearchString = String.join(" ", Arrays.stream(searchString.split(" ")).map(s -> String.format("<em>%s</em>", s)).toArray(String[]::new));
        assertThat(
                // When
                () -> asInspectableResponse(httpClient.get(SEARCH_SERVICE_BASE_URL, headers(),
                        queryParameters(
                                queryParameter("query", searchString),
                                queryParameter("from", 0),
                                queryParameter("size", 50)
                        ))),
                // Then
                eventuallySatisfies(is(anHttpResponseWith(
                        both(statusCode(OK))
                                .and(bodySatisfying(
                                        allOf(
                                                hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s')]", PAPER_52.getId(), PAPER_52.getTitle(), RealSsrnUsers.USER_54.getId(), expectedHighlightedSearchString, RealSsrnUsers.USER_1341.getId(), RealSsrnUsers.USER_1341.getPublicDisplayName())),
                                                hasJsonPath(format("$.results[?(@.@type == 'Author' && @.id == '%s' && @.name == '%s')]", RealSsrnUsers.USER_54.getId(), expectedHighlightedSearchString)),
                                                not(hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s')]", PAPER_42.getId(), PAPER_42.getTitle(), RealSsrnUsers.USER_35974.getId(), RealSsrnUsers.USER_35974.getPublicDisplayName(), RealSsrnUsers.USER_35976.getId(), RealSsrnUsers.USER_35976.getPublicDisplayName()))),
                                                not(hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s' && @.title == '%s' && @.authors[0].id == '%s' && @.authors[0].name == '%s' && @.authors[1].id == '%s' && @.authors[1].name == '%s' && @.authors[2].id == '%s' && @.authors[2].name == '%s')]", PAPER_20005.getId(), PAPER_20005.getTitle(), BILLY_BOB.getId(), BILLY_BOB.getPublicDisplayName(), LUCY_JONES.getId(), LUCY_JONES.getPublicDisplayName(), JOHN_DOE.getId(), JOHN_DOE.getPublicDisplayName()))),
                                                not(hasJsonPath(format("$.results[?(@.@type == 'Paper' && @.id == '%s')]", PAPER_45.getId())))
                                        )))))
                ).within(180, SECONDS, checkingEvery(1, SECONDS))
        );
    }

    private static boolean isNonSearchablePaper(SsrnPaper ssrnPaper) {
        return "IN DRAFT".equals(ssrnPaper.getSubmissionStage()) || "DELETED".equals(ssrnPaper.getSubmissionStage()) || "REJECTED".equals(ssrnPaper.getSubmissionStage());
    }

    private static HttpClient createHttpClient() {
        return new HttpClient("search api contract tests", new HttpClientConfiguration() {
            @Override
            public int connectionTimeoutMillisseconds() {
                return 10000;
            }

            @Override
            public int readTimeoutMilliseconds() {
                return 10000;
            }

            @Override
            public Level logLevel() {
                return Level.INFO;
            }

            @Override
            public Boolean logEntity() {
                return true;
            }

            @Override
            public int maxEntityBytesToLog() {
                return 4096;
            }
        });
    }


}
