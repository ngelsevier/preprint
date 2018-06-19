package com.ssrn.frontend.website.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class SearchApiSearchEngine implements SearchEngine {
    private final static Logger LOGGER = LoggerFactory.getLogger(SearchApiSearchEngine.class);

    private final WebTarget searchApiWebTarget;
    private final int searchResultPageSize;

    public SearchApiSearchEngine(String baseUrl, Client client, int searchResultPageSize) {
        this.searchResultPageSize = searchResultPageSize;
        searchApiWebTarget = client.target(baseUrl);
    }

    @Override
    public SearchResults findItemsMatching(String query, int searchResultsOffsetFrom) {
        Response response;
        try {
            response = searchApiWebTarget
                    .queryParam("query", query)
                    .queryParam("from", searchResultsOffsetFrom)
                    .queryParam("size", searchResultPageSize)
                    .request()
                    .get();
        } catch (ProcessingException e) {
            LOGGER.error("Exception thrown whilst querying search api", e);
            return new SearchResults(0, new BaseSearchResult[0]);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            return response.readEntity(SearchResults.class);
        } else {
            return new SearchResults(0, new BaseSearchResult[0]);
        }
    }
}
