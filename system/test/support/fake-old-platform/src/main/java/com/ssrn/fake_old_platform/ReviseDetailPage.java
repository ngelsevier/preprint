package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class ReviseDetailPage {

    private final PaperRepository paperRepository;

    ReviseDetailPage(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    @GET
    @Path("/submissions/ReviseDetail.cfm")
    public ReviseDetailPageView getReviseDetailPage(@QueryParam("AbstractID") int abstractId) {

        Paper paper = paperRepository.getById(abstractId);
        if (paper.submissionStageIsSubmitted()) {
            paper.submissionStageChangedTo("UNDER REVIEW");
            paperRepository.save(paper);
        }
        return new ReviseDetailPageView(abstractId);
    }

    @GET
    @Path("/submissions/ReviseDetailTreeFrameContent.cfm")
    public ReviseDetailTreeFrameContentPageView getReviseDetailTreeFrameContentPage(@QueryParam("AbstractID") int abstractId) {
        return new ReviseDetailTreeFrameContentPageView(abstractId);
    }

    @POST
    @Path("/submissions/ReviseDB.cfm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View approveMinorRevision2(@FormParam("AbstractID") String abstractId,
                                      @FormParam("2990376") String appliedSciencesCheckBox,
                                      @QueryParam("abid") int abid) {

        if (abid > 0) {
            Paper revisedPaper = paperRepository.getById(abid);
            if (revisedPaper.getSubmissionStage().equals("UNDER REVIEW")) {
                revisedPaper.submissionStageChangedTo("APPROVED");
                paperRepository.save(revisedPaper);

                Paper originalPaper = paperRepository.getById(revisedPaper.getOriginalAbstractId());
                originalPaper.changeTitleTo(revisedPaper.getTitle(), null);
                paperRepository.save(originalPaper);

                originalPaper.reorderAuthors(revisedPaper.getAuthorIds());
                paperRepository.save(originalPaper);

                revisedPaper.delete();
                paperRepository.save(revisedPaper);
                paperRepository.remove(revisedPaper.getId());

            }
            return new RevisionQueuePageView(paperRepository);

        } else {
            return new PaperRevisionAcceptedPageView(abstractId);
        }
    }
}
