package com.ssrn.fake_old_platform;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PermitAll
@Path("/rest/papers")
@Produces("application/json; charset=utf-8")
public class PaperEntitiesFeedResource {

    private final PaperEntitiesFeedService paperEntitiesFeedService;

    PaperEntitiesFeedResource(PaperEntitiesFeedService paperEntitiesFeedService) {
        this.paperEntitiesFeedService = paperEntitiesFeedService;
    }

    @GET
    public Response getEntrypointUrlWorkingPage(@QueryParam("afterId") Optional<Integer> paperId) {
        List<Paper> papersInAscendingIdOrder = paperEntitiesFeedService.getStreamOfPapers(paperId)
                .limit(Service.ENTITIES_FEED_ITEMS_PER_PAGE)
                .collect(Collectors.toList());

        return Response.ok().entity(new PaperEntitiesPage(papersInAscendingIdOrder)).build();
    }

}
