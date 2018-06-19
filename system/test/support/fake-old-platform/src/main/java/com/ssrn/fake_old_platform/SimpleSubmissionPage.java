package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssrn.test.support.golden_data.FakeSsrnUsers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class SimpleSubmissionPage {
    private final SequentialIdGenerator sequentialAbstractIdGenerator;
    private PaperRepository paperRepository;
    private final String authorId;
    private final String firstAdditionalAuthorAccountId;
    private final String secondAdditionalAuthorAccountId;
    private final String thirdAdditionalAuthorAccountId;
    private ParticipantRepository participantRepository;

    SimpleSubmissionPage(SequentialIdGenerator sequentialAbstractIdGenerator, PaperRepository paperRepository, String authorId, String firstAdditionalAuthorAccountId, String secondAdditionalAuthorAccountId, String thirdAddtionalAuthorAccountId, ParticipantRepository participantRepository) {
        this.sequentialAbstractIdGenerator = sequentialAbstractIdGenerator;
        this.paperRepository = paperRepository;
        this.authorId = authorId;
        this.firstAdditionalAuthorAccountId = firstAdditionalAuthorAccountId;
        this.secondAdditionalAuthorAccountId = secondAdditionalAuthorAccountId;
        this.thirdAdditionalAuthorAccountId = thirdAddtionalAuthorAccountId;
        this.participantRepository = participantRepository;
    }

    @GET
    @Path("/submissions/SimpleSubmission.cfm")
    public SimpleSubmissionPageView getSimpleSubmissionPage(@QueryParam("AbstractID") int abstractId, @QueryParam("revisionAbstractID") int revisionAbstractId) {
        return new SimpleSubmissionPageView(abstractId, revisionAbstractId, FakeSsrnUsers.JOHN_DOE.getId());
    }

    @GET
    @Path("/submissions/CreateNewAbstract.cfm")
    public Response createNewAbstract() {
        Participant participant = participantRepository.getById(Integer.parseInt(authorId)).get();

        int abstractId = sequentialAbstractIdGenerator.getNextId();
        paperRepository.save(new Paper(abstractId, Integer.toString(abstractId), null, false, false, false, false, false, new int[]{Integer.parseInt(authorId)}, "IN DRAFT"));
        participant.addToPaper(abstractId, null, false);
        return Response
                .seeOther(URI.create(String.format("/submissions/SimpleSubmission.cfm?AbstractID=%s", abstractId)))
                .build();
    }

    @GET
    @Path("/submissions/StartRevision.cfm")
    public Response startRevision(@QueryParam("AbstractID") int abstractId) {

        Paper originalPaper = paperRepository.getById(abstractId);
        int revisionAbstractId = sequentialAbstractIdGenerator.getNextId();

        Paper paperRevision = new Paper(
                revisionAbstractId,
                originalPaper.getTitle(),
                originalPaper.getKeywords(),
                false,
                originalPaper.isPaperPrivate(),
                originalPaper.isConsideredIrrelevant(),
                originalPaper.isPaperRestricted(),
                originalPaper.isPaperTolerated(),
                originalPaper.getAuthorIds(),
                "IN DRAFT");

        paperRevision.setOriginalAbstractId(abstractId);

        Participant participant = participantRepository.getById(originalPaper.getAuthorIds()[0]).get();

        paperRepository.save(paperRevision);
        participant.addToPaper(revisionAbstractId, null, false);

        return Response
                .seeOther(URI.create(String.format("/submissions/SimpleSubmission.cfm?AbstractID=%s&revisionAbstractID=%s", abstractId, revisionAbstractId)))
                .build();
    }

    @POST
    @Path("/submissions/change-title")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeTitle(ChangeTitleRequest changeTitleRequest) {
        Paper paper = paperRepository.getById(changeTitleRequest.getAbstractId());
        paper.changeTitleTo(changeTitleRequest.getTitle());
        paperRepository.save(paper);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/change-keywords")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeKeywords(ChangeKeywordsRequest changeKeywordsRequest) {
        Paper paper = paperRepository.getById(changeKeywordsRequest.getAbstractId());
        paper.changeKeywordsTo(changeKeywordsRequest.getKeywords());
        paperRepository.save(paper);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/make-paper-private")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makePaperPrivate(int abstractId) {
        Paper paper = paperRepository.getById(abstractId);
        paper.makePaperPrivate();
        paperRepository.save(paper);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/make-paper-public")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makePaperPublic(int abstractId) {
        Paper paper = paperRepository.getById(abstractId);
        paper.makePaperPublic();
        paperRepository.save(paper);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/add-author")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAuthor(AuthorChangeRequest authorChangeRequest) {
        Participant participant = participantRepository.getById(Integer.parseInt(authorChangeRequest.authorId)).get();
        participant.addToPaper(authorChangeRequest.getAbstractId(), null, false);
        participantRepository.save(participant);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/remove-author")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAuthor(AuthorChangeRequest authorChangeRequest) {
        Participant participant = participantRepository.getById(Integer.parseInt(authorChangeRequest.authorId)).get();
        participant.removeFromPaper(authorChangeRequest.getAbstractId());
        participantRepository.save(participant);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/move-author")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveAuthor(MoveAuthorChangeRequest moveAuthorChangeRequest) {
        Paper paper = paperRepository.getById(moveAuthorChangeRequest.getAbstractId());
        int authorId = Integer.parseInt(moveAuthorChangeRequest.authorId);

        List<Integer> authors = Arrays.stream(paper.getAuthorIds()).boxed().collect(Collectors.toList());
        int pos = authors.indexOf(authorId);
        authors.remove(pos);
        LinkedList<Integer> orderedAuthors = new LinkedList<>(authors);
        int newPos = "down".equals(moveAuthorChangeRequest.direction) ? pos + 1 : pos - 1;
        orderedAuthors.add(newPos, authorId);
        paper.reorderAuthors(orderedAuthors.stream().mapToInt(i -> i).toArray());
        paperRepository.save(paper);

        return Response.ok().build();
    }

    @POST
    @Path("/submissions/SimpleSubmitDB.cfm")
    public SubmissionConfirmationView submitAbstract(@FormParam("AbstractID") String abstractId,@FormParam("ParticipantID") String participantId) {
        Paper paper = paperRepository.getById(Integer.parseInt(abstractId));
        paper.submissionStageChangedTo("SUBMITTED");
        paperRepository.save(paper);
        return new SubmissionConfirmationView(participantId);
    }

    @GET
    @Path("/submissions/iFrameAuthors2.cfm")
    public SimpleSubmissionAuthorsListView getAuthorsList(@QueryParam("AbstractID") int abstractId, @QueryParam("AuthorID") String ssrnAccountId) {
        Paper paper = paperRepository.getById(abstractId);
        return new SimpleSubmissionAuthorsListView(abstractId, ssrnAccountId, firstAdditionalAuthorAccountId, secondAdditionalAuthorAccountId, thirdAdditionalAuthorAccountId, paper == null ? new String[]{} :
                Arrays.stream(paper.getAuthorIds()).mapToObj(String::valueOf).toArray(String[]::new));
    }

    public static class ChangeTitleRequest {
        private int abstractId;
        private final String title;

        @JsonCreator
        public ChangeTitleRequest(@JsonProperty("abstractId") int abstractId,
                                  @JsonProperty("title") String title) {
            this.abstractId = abstractId;
            this.title = title;
        }

        public int getAbstractId() {
            return abstractId;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class ChangeKeywordsRequest {
        private int abstractId;
        private final String keywords;

        @JsonCreator
        public ChangeKeywordsRequest(@JsonProperty("abstractId") int abstractId,
                                     @JsonProperty("keywords") String keywords) {
            this.abstractId = abstractId;
            this.keywords = keywords;
        }

        public int getAbstractId() {
            return abstractId;
        }

        public String getKeywords() {
            return keywords;
        }
    }

    public static class AuthorChangeRequest {

        private String authorId;
        private int abstractId;

        @JsonCreator
        public AuthorChangeRequest(@JsonProperty("abstractId") int abstractId,
                                   @JsonProperty("authorId") String authorId) {
            this.authorId = authorId;
            this.abstractId = abstractId;
        }

        public int getAbstractId() {
            return abstractId;
        }

        public String getAuthorId() {
            return authorId;
        }

        @Override
        public String toString() {
            return "AuthorChangeRequest{" +
                    "authorId=" + authorId +
                    ", abstractId=" + abstractId +
                    '}';
        }
    }

    public static class MoveAuthorChangeRequest {

        private String direction;
        private String authorId;
        private int abstractId;

        @JsonCreator
        public MoveAuthorChangeRequest(@JsonProperty("direction") String direction, @JsonProperty("abstractId") int abstractId,
                                       @JsonProperty("authorId") String authorId) {
            this.direction = direction;
            this.authorId = authorId;
            this.abstractId = abstractId;
        }

        public String getDirection() {
            return direction;
        }

        public int getAbstractId() {
            return abstractId;
        }

        public String getAuthorId() {
            return authorId;
        }

        @Override
        public String toString() {
            return "MoveAuthorChangeRequest{" +
                    "direction='" + direction + '\'' +
                    ", authorId='" + authorId + '\'' +
                    ", abstractId=" + abstractId +
                    '}';
        }
    }
}
