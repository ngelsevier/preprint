package com.ssrn.search.author_updates_subscriber.fake_authors_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/metadata")
public class MetadataResource {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private KinesisStreamClient kinesisStreamClient;

    MetadataResource(KinesisStreamClient kinesisStreamClient) {
        this.kinesisStreamClient = kinesisStreamClient;
    }

    @POST
    @Path("/streams/{streamName}/emitted-author-updates")
    public Response addEmittedAuthorUpdate(@PathParam("streamName") String streamName, AuthorServiceAuthorUpdate authorUpdate) {
        return handleRequestToAddEmittedAuthorUpdate(authorUpdate.getAuthor().getId(), asJson(authorUpdate, objectMapper), streamName);
    }

    @POST
    @Path("/streams/{streamName}/emitted-unprocessable-author-updates")
    public Response addEmittedUnprocessableAuthorUpdate(@PathParam("streamName") String streamName) {
        return handleRequestToAddEmittedAuthorUpdate(UUID.randomUUID().toString(),"junk", streamName);
    }

    private Response handleRequestToAddEmittedAuthorUpdate(String id, String authorUpdateJson, String streamName) {
        try {
            kinesisStreamClient.writeRecordToStream(streamName, id, authorUpdateJson);
            return Response.status(Response.Status.OK).build();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static String asJson(AuthorServiceAuthorUpdate authorUpdate, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(authorUpdate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

