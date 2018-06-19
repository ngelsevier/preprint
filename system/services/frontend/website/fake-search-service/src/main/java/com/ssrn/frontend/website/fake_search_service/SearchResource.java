package com.ssrn.frontend.website.fake_search_service;

import org.apache.commons.lang3.ArrayUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@Path("/")
public class SearchResource {
    private final List<IndexedPaper> indexedPapers;
    private final List<IndexedAuthor> indexedAuthors;
    private final Queue<OverriddenResponse> overriddenResponses;

    public SearchResource(List<IndexedPaper> indexedPapers, List<IndexedAuthor> indexedAuthors, Queue<OverriddenResponse> overriddenResponses) {
        this.indexedPapers = indexedPapers;
        this.indexedAuthors = indexedAuthors;
        this.overriddenResponses = overriddenResponses;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@QueryParam("query") String query, @QueryParam("size") int pageSize, @QueryParam("from") int from) {
        OverriddenResponse overriddenResponse = overriddenResponses.poll();

        if (overriddenResponse != null) {
            return Response.status(overriddenResponse.getStatusCode()).build();
        }

        BaseSearchResult[] paperSearchResults = indexedPapers
                .stream()
                .filter(paper -> paper.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        (paper.getKeywords() != null && paper.getKeywords().toLowerCase().contains(query.toLowerCase())) ||
                        Arrays.stream(paper.getAuthors())
                                .anyMatch(author -> author.getName() != null && author.getName().toLowerCase().contains(query.toLowerCase())))
                .map(paper -> new PaperSearchResult(paper.getId(), paper.getTitle(), paper.getKeywords(), paper.getAuthors(), query))
                .toArray(BaseSearchResult[]::new);

        BaseSearchResult[] authorSearchResults = indexedAuthors
                .stream()
                .filter(author -> author.getName().toLowerCase().contains(query.toLowerCase()))
                .map(author -> new AuthorSearchResult(author.getId(), author.getName(), query))
                .toArray(BaseSearchResult[]::new);

        BaseSearchResult[] searchResults = ArrayUtils.addAll(paperSearchResults, authorSearchResults);
        return Response
                .ok(new SearchResults(searchResults.length,
                        Arrays.copyOfRange(searchResults, from, from + pageSize <= searchResults.length ? from + pageSize : searchResults.length)))
                .build();

    }

}
