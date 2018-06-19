package com.ssrn.frontend.website.fake_search_service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Queue;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/metadata")
public class MetadataResource {
    private final List<IndexedPaper> indexedPapers;
    private final Queue<OverriddenResponse> overriddenResponses;
    private final List<IndexedAuthor> indexedAuthors;
    private final Queue<Boolean> overriddenReturnNewSearchResults;

    MetadataResource(List<IndexedPaper> indexedPapers, Queue<OverriddenResponse> overriddenResponses, List<IndexedAuthor> indexedAuthors, Queue<Boolean> overriddenReturnNewSearchResults) {
        this.indexedPapers = indexedPapers;
        this.overriddenResponses = overriddenResponses;
        this.indexedAuthors = indexedAuthors;
        this.overriddenReturnNewSearchResults = overriddenReturnNewSearchResults;
    }

    @POST
    @Path("/indexed-papers")
    public Response createIndexedPaper(IndexedPaper paper) {
        indexedPapers.add(paper);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/indexed-authors")
    public Response createIndexedAuthor(IndexedAuthor author) {
        indexedAuthors.add(author);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/overridden-responses")
    public Response createOverriddenResponse(OverriddenResponse overriddenResponse) {
        overriddenResponses.add(overriddenResponse);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/reset")
    public Response reset() {
        indexedPapers.clear();
        indexedAuthors.clear();
        overriddenResponses.clear();
        return Response.status(Response.Status.OK).build();
    }

}
