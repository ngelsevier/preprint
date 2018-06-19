package com.ssrn.search.papers_consumer.fake_papers_service;

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
    @Path("/streams/{streamName}/emitted-papers")
    public Response addEmittedPaper(@PathParam("streamName") String streamName, PaperServicePaper paper) {
        return handleRequestToEmitKinesisRecordOnTo(streamName, paper.getId(), asJson(paper, objectMapper));
    }

    @POST
    @Path("/streams/{streamName}/emitted-unprocessable-papers")
    public Response addEmittedUnprocessablePaper(@PathParam("streamName") String streamName) {
        return handleRequestToEmitKinesisRecordOnTo(streamName, UUID.randomUUID().toString(), "junk");
    }

    private Response handleRequestToEmitKinesisRecordOnTo(String streamName, String partitionKey, String data) {
        try {
            kinesisStreamClient.writeRecordToStream(streamName, partitionKey, data);
            return Response.status(Response.Status.OK).build();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static String asJson(Object jsonObject, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

