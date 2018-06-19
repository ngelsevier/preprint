package com.ssrn.fake_old_platform;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class ModifySubmissionPopup {

    private PaperRepository paperRepository;
    private final ParticipantRepository participantRepository;

    public ModifySubmissionPopup(PaperRepository paperRepository, ParticipantRepository participantRepository) {
        this.paperRepository = paperRepository;
        this.participantRepository = participantRepository;
    }

    @GET
    @Path("/submissions/ProcessRemove.cfm")
    public ModifySubmissionPopupView getModifySubmissionPopup(@QueryParam("abid") int abstractId) {
        return new ModifySubmissionPopupView(abstractId);
    }

    @POST
    @Path("/submissions/ProcessRemove.cfm")
    public void modifySubmission(@QueryParam("abid") int abstractId, @FormParam("txtOption") String deactivatePaper) {
        if (deactivatePaper != null) {
            Paper paper = paperRepository.getById(abstractId);
            paper.submissionStageChangedTo("REJECTED");
            paperRepository.save(paper);
            unregisterAuthorWith(paper);
        }

    }

    private void unregisterAuthorWith(Paper paper) {
        Arrays.stream(paper.getAuthorIds())
                .mapToObj(id -> participantRepository.getById(id).get())
                .filter(participant ->
                        Arrays.stream(paperRepository.getPapersWrittenBy(participant))
                                .filter(p -> p.getId() != paper.getId())
                                .allMatch(p -> !p.hasSearchableSubmissionStage()))
                .forEach(participant -> participant.emitAuthorEvent("UNREGISTERED", null));
    }

}
