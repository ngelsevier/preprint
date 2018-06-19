package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/healthcheck")
public class HealthcheckResource {
    @GET
    public Response getHealthcheck() {
        return Response.ok().build();
    }
}
