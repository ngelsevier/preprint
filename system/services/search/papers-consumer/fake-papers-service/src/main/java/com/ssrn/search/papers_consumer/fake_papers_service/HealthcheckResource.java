package com.ssrn.search.papers_consumer.fake_papers_service;

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
