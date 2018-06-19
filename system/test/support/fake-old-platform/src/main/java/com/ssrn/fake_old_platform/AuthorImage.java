package com.ssrn.fake_old_platform;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/sol3/cf_dev/AuthorProfilePicture.cfm")
public class AuthorImage {

    AuthorImage() { }

    @GET
    @Produces("image/png")
    public Response getAuthorImage(@QueryParam("per_id") int authorId) {
        return Response.seeOther(URI.create("/Images/authorThumbnail.png")).type("image/png").build();
    }
}
