package com.ssrn.fake_old_platform;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@PermitAll
@Path("/rest/meta")
@Produces("application/json; charset=utf-8")
public class AuthorPaperDisaocciationResource {
    private final PaperRepository paperRepository;
    private final ParticipantRepository participantRepository;

    AuthorPaperDisaocciationResource(PaperRepository paperRepository, ParticipantRepository participantRepository) {
        this.paperRepository = paperRepository;
        this.participantRepository = participantRepository;
    }

    @Path("resetParticipant/{authorId}")
    @GET
    public Response resetParticpant(@PathParam(value = "authorId") Integer authorId) {
        participantRepository
                .getById(authorId)
                .ifPresent(participant -> Arrays.stream(paperRepository.getPapersWrittenBy(participant))
                        .forEach(paper -> participant.removeFromPaper(paper.getId()))
                );

        return Response.ok().build();
    }

}
