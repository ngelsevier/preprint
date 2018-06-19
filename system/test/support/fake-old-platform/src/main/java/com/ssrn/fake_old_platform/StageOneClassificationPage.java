package com.ssrn.fake_old_platform;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class StageOneClassificationPage {

    private final PaperRepository paperRepository;
    private final ParticipantRepository participantRepository;

    StageOneClassificationPage(PaperRepository paperRepository, ParticipantRepository participantRepository) {
        this.paperRepository = paperRepository;
        this.participantRepository = participantRepository;
    }

    @GET
    @Path("/submissions/Stage1queue.cfm")
    public StageOneClassificationPageView getStageOneClassificationPage() {
        return new StageOneClassificationPageView();
    }

    @POST
    @Path("/submissions/Stage1QueueSearch.cfm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public StageOneClassificationSearchPageView getStageOneSearchResults(@FormParam("txtAbSearch") String abstractId) {
        return new StageOneClassificationSearchPageView(abstractId);
    }

    @GET
    @Path("/submissions/Stage1Class.cfm")
    public StageOneClassificationAbstractPageView getStageOneAbstractClassification(@QueryParam("AbstractID") String abstractId) {
        Paper paper = paperRepository.getById(Integer.parseInt(abstractId));
        if (paper.submissionStageIsSubmitted()) {
            paper.submissionStageChangedTo("UNDER REVIEW");
        }
        return new StageOneClassificationAbstractPageView(abstractId);
    }

    @GET
    @Path("/submissions/Stage1ClassTreeFrameContent.cfm")
    public StageOneClassificationAbstractTreeFrameContentPageView getStageOneAbstractClassificationTreeFrameContent(@QueryParam("AbstractID") String abstractId) {
        return new StageOneClassificationAbstractTreeFrameContentPageView(abstractId, paperRepository.getById(Integer.parseInt(abstractId)).isConsideredIrrelevant());
    }

    @POST
    @Path("/submissions/Stage1ClassDB.cfm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response foo(@FormParam("AbstractID") String abstractId,
                        @FormParam("NO_NET_CLASS") String consideredIrrelevantCheckboxValue,
                        @FormParam("cboStatus") String submissionStage
    ) throws URISyntaxException {
        Paper paper = paperRepository.getById(Integer.parseInt(abstractId));

        if (hasPaperRelevancyChanged(paper, consideredIrrelevantCheckboxValue)) {
            if (hasPaperBeenConsideredIrrelevant(consideredIrrelevantCheckboxValue)) {
                paper.considerPaperIrrelevant();
            } else {
                paper.considerPaperRelevant();
            }
        }

        if ("DELETED".equals(submissionStage)) {
            paper.submissionStageChangedTo("DELETED");
        } else if ("REMOVED".equals(submissionStage)) {
            paper.submissionStageChangedTo("REJECTED");
        } else if ("APPROVED-RESTRICTED".equals(submissionStage)) {
            paper.submissionStageChangedTo("APPROVED");
            paper.makePaperRestricted();
        } else if ("APPROVED".equals(submissionStage)) {
            if (paper.isPaperRestricted()) {
                paper.makePaperUnrestricted();
            }
            if (!paper.submissionStageIsApproved()) {
                paper.submissionStageChangedTo("APPROVED");
            }
        }

        paperRepository.save(paper);
        registerOrDeregisterAuthorWith(paper);

        return Response.seeOther(new URI("/submissions/Stage1queue.cfm")).build();
    }

    private void registerOrDeregisterAuthorWith(Paper paper) {
        if (paper.hasSearchableSubmissionStage()) {
            updateAuthorRegistration(paper, "REGISTERED");
        } else {
            updateAuthorRegistration(paper, "UNREGISTERED");
        }
    }

    private void updateAuthorRegistration(Paper paper, String registrationChangeEvent) {
        Arrays.stream(paper.getAuthorIds())
                .mapToObj(id -> participantRepository.getById(id).get())
                .filter(participant ->
                        Arrays.stream(paperRepository.getPapersWrittenBy(participant))
                                .filter(p -> p.getId() != paper.getId())
                                .allMatch(p -> !p.hasSearchableSubmissionStage()))
                .forEach(participant -> participant.emitAuthorEvent(registrationChangeEvent, null));
    }

    private boolean hasPaperBeenConsideredIrrelevant(String consideredIrrelevantCheckboxValue) {
        return "on".equals(consideredIrrelevantCheckboxValue);
    }

    private boolean hasPaperRelevancyChanged(Paper paper, String consideredIrrelevantCheckboxValue) {
        return paper.isConsideredIrrelevant() != hasPaperBeenConsideredIrrelevant(consideredIrrelevantCheckboxValue);
    }
}
