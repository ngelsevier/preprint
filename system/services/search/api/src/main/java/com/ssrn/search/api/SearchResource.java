package com.ssrn.search.api;

import com.ssrn.search.shared.Library;
import com.ssrn.search.shared.SearchResults;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    private final Library library;

    SearchResource(Library library) {
        this.library = library;
    }

    @GET
    @Produces("application/json; charset=utf-8")
    public Response search(@QueryParam("query") String query, @QueryParam("from") int from, @QueryParam("size") int size) {
        SearchResults searchResults = library.searchForItemsMatching(query, from, size);
        return Response.ok().entity(searchResults).build();
    }

}
