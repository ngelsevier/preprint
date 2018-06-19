package com.ssrn.logging.fake_logging_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class LogsResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(LogsResource.class);

    @POST
    @Path("/logs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createLogMessage(String message) {
        LOGGER.info(message + "\n" + message);
        return Response.ok().build();
    }

    @POST
    @Path("/logException")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createException(String message) {
        try {
            throw new RuntimeException(message);
        } catch (Exception e) {
            LOGGER.error(message, e);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/throwException")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUnhandledException(String message) {
        throw new RuntimeException(message);
    }
}
