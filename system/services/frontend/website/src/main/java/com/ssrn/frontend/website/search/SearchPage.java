package com.ssrn.frontend.website.search;

import io.dropwizard.views.View;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.function.Function;

@PermitAll
@Path("/{a:search|fastsearch}")
@Produces("text/html; charset=utf-8")
public class SearchPage {

    private final SearchEngine searchEngine;
    private final Function<BaseSearchResult, String> searchResultToArticlePageUrlMapper;
    private final Function<SearchResultAuthor, String> searchResultAuthorToAuthorPageUrlMapper;
    private final Function<SearchResultAuthor, String> searchResultAuthorToAuthorImageUrlMapper;
    private final String authBaseUrl;
    private final int searchResultPageSize;

    public SearchPage(SearchEngine searchEngine, String articlePageBaseUrl, String authorProfilePageBaseUrl, String authorImageBaseUrl, String authBaseUrl, int searchResultPageSize) {
        this.searchEngine = searchEngine;

        searchResultToArticlePageUrlMapper = searchResult -> UriBuilder
                .fromPath(articlePageBaseUrl)
                .path(String.format("abstract=%s", searchResult.getId()))
                .toString();

        searchResultAuthorToAuthorPageUrlMapper = author -> UriBuilder
                .fromPath(authorProfilePageBaseUrl)
                .path(String.format("author=%s", author.getId()))
                .toString();

        searchResultAuthorToAuthorImageUrlMapper = author -> UriBuilder
                .fromPath(authorImageBaseUrl)
                .path("sol3/cf_dev/AuthorProfilePicture.cfm")
                .queryParam("per_id", author.getId())
                .toString();

        this.authBaseUrl = authBaseUrl;
        this.searchResultPageSize = searchResultPageSize;
    }

    @GET
    public View get(@Context UriInfo uriInfo, @Context HttpHeaders headers, @QueryParam("query") String query, @QueryParam("from") int searchResultsOffsetFrom) {
        if (query == null) {
            return new SearchPageView(authBaseUrl);
        }

        SearchResults searchResults = searchEngine.findItemsMatching(query, searchResultsOffsetFrom);

        return new SearchResultsPageView(searchResults.getSearchResults(), query, searchResultToArticlePageUrlMapper, searchResultAuthorToAuthorPageUrlMapper, searchResultAuthorToAuthorImageUrlMapper, authBaseUrl,
                searchResults.getTotalNumberOfResults(), searchResultsOffsetFrom, searchResultPageSize);
    }
}
