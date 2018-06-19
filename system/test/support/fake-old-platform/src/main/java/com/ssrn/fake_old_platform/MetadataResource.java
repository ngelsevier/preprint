package com.ssrn.fake_old_platform;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;

@Path("/metadata")
public class MetadataResource {

    private final Queue<ResponseDelay> responseDelays;
    private final SequentialIdGenerator paperSequentialAbstractIdGenerator;
    private final Queue<OverriddenResponse> overriddenResponses;
    private final EventLog authorEventLog;
    private final ParticipantRepository participantRepository;
    private final PaperEntitiesFeedService paperEntitiesFeedService;
    private PaperRepository paperRepository;
    private final EventLog paperEventLog;
    private AuthorEntitiesFeedService authorEntitiesFeedService;
    private SequentialIdGenerator participantSequentialIdGenerator;

    MetadataResource(Queue<ResponseDelay> responseDelays, SequentialIdGenerator paperSequentialAbstractIdGenerator, PaperRepository paperRepository, SequentialIdGenerator participantSequentialIdGenerator, EventLog paperEventLog, PaperEntitiesFeedService paperEntitiesFeedService, AuthorEntitiesFeedService authorEntitiesFeedService, Queue<OverriddenResponse> overriddenResponses, EventLog authorEventLog, ParticipantRepository participantRepository) {

        this.responseDelays = responseDelays;
        this.paperSequentialAbstractIdGenerator = paperSequentialAbstractIdGenerator;
        this.paperRepository = paperRepository;
        this.participantSequentialIdGenerator = participantSequentialIdGenerator;
        this.paperEventLog = paperEventLog;
        this.paperEntitiesFeedService = paperEntitiesFeedService;
        this.authorEntitiesFeedService = authorEntitiesFeedService;
        this.overriddenResponses = overriddenResponses;
        this.authorEventLog = authorEventLog;
        this.participantRepository = participantRepository;
    }

    @PUT
    @Path("/next-response-millisecond-delay")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setNextResponseMillisecondDelay(long millisecondDelay) {
        responseDelays.add(new ResponseDelay(millisecondDelay));
        return Response.ok().build();
    }

    @POST
    @Path("/add-historical-paper")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addHistoricalPaper(AddHistoricPaperRequest addHistoricPaperRequest) {
        int abstractId = paperSequentialAbstractIdGenerator.getNextId();
        Paper paper = new Paper(abstractId,
                addHistoricPaperRequest.getTitle(),
                addHistoricPaperRequest.getKeywords(),
                true,
                addHistoricPaperRequest.isPaperPrivate(),
                addHistoricPaperRequest.isPaperIrrelevant(),
                addHistoricPaperRequest.isPaperRestricted(),
                addHistoricPaperRequest.getSubmissionStage());
        paperRepository.save(paper);

        Arrays.stream(addHistoricPaperRequest.getAuthorIds())
                .mapToInt(Integer::parseInt)
                .forEach(authorId -> {
                    Participant participant = participantRepository.getById(authorId).orElseGet(() -> {
                        Participant p = new Participant("default@email.com", authorId, "Default Name", authorEventLog, paperRepository);
                        participantRepository.save(p);
                        return p;
                    });

                    participant.addToPaper(abstractId, null, true);
                });

        return Response.status(Response.Status.CREATED).entity(paper).build();
    }

    @POST
    @Path("/add-historical-author")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addHistoricalAuthor(AddHistoricAuthorRequest addHistoricAuthorRequest) {
        Paper paper = new Paper(paperSequentialAbstractIdGenerator.getNextId(),
                "Paper Written By Historic Author",
                null,
                true,
                null,
                false,
                false,
                false,
                false,
                new int[0],
                "SUBMITTED");
        paperRepository.save(paper);

        Participant participant = new Participant(null, participantSequentialIdGenerator.getNextId(), addHistoricAuthorRequest.getName(), authorEventLog, paperRepository);
        participantRepository.save(participant);
        participant.addToPaper(paper.getId(), null, true);

        return Response.status(Response.Status.CREATED).entity(participant.getAccountId()).build();
    }

    @POST
    @Path("/add-new-paper")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewPaper(Paper paper) {
        paperRepository.save(paper);
        return Response.status(Response.Status.CREATED).entity(paper).build();
    }

    @GET
    @Path("/papers/{paperId}/events")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsForPaper(@PathParam("paperId") String paperId) {
        return Response.ok().entity(paperEventLog.getListOfEventsContainingEntityId(paperId)).build();
    }

    @GET
    @Path("/next-abstract-id")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getNextAbstractId() {
        return Response.ok().entity(paperSequentialAbstractIdGenerator.getNextId()).build();
    }

    @GET
    @Path("/next-author-id")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getNextAuthorId() {
        return Response.ok().entity(participantSequentialIdGenerator.getNextId()).build();
    }

    @GET
    @Path("/number-of-papers-in-entity-feed-after-paper")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getNextAbstractId(@QueryParam("paperId") Integer paperId) {
        return Response.ok().entity(paperEntitiesFeedService.getStreamOfPapers(Optional.of(paperId)).count()).build();
    }

    @GET
    @Path("/number-of-authors-in-entity-feed-after-author")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getNextAuthorId(@QueryParam("authorId") Integer authorId) {
        return Response.ok().entity(authorEntitiesFeedService.getStreamOfAuthors(Optional.of(authorId)).count()).build();
    }

    @POST
    @Path("/overridden-responses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enqueueOverriddenResponse(OverriddenResponse overriddenResponse) {
        overriddenResponses.add(overriddenResponse);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/reset-overrides")
    @Produces(MediaType.TEXT_PLAIN)
    public Response resetOverrides() {
        overriddenResponses.clear();
        responseDelays.clear();
        return Response.ok().build();
    }



}
