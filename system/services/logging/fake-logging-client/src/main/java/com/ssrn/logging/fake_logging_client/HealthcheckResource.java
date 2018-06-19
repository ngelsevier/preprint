package com.ssrn.logging.fake_logging_client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/heartbeat")
public class HealthcheckResource {
    @GET
    public Response getHealthcheck() {
        return Response.ok().build();
    }
}
