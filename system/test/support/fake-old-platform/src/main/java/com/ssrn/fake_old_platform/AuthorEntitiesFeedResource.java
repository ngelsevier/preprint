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
@Path("/rest/authors")
@Produces("application/json; charset=utf-8")
public class AuthorEntitiesFeedResource {
    private AuthorEntitiesFeedService authorEntitiesFeedService;

    AuthorEntitiesFeedResource(AuthorEntitiesFeedService authorEntitiesFeedService) {
        this.authorEntitiesFeedService = authorEntitiesFeedService;
    }


    @GET
    public Response getEntrypointUrlWorkingPage(@QueryParam("afterId") Optional<Integer> authorId) {
        List<Author> authorsInAscendingIdOrder = authorEntitiesFeedService.getStreamOfAuthors(authorId)
                .limit(Service.ENTITIES_FEED_ITEMS_PER_PAGE)
                .collect(Collectors.toList());

        return Response.ok().entity(new AuthorEntitiesPage(authorsInAscendingIdOrder)).build();
    }

}
