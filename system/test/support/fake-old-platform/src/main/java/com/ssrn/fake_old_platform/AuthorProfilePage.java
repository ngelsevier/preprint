package com.ssrn.fake_old_platform;

import javax.ws.rs.*;
import java.util.Optional;

@Path("/author={author}")
@Produces("text/html; charset=utf-8")
public class AuthorProfilePage {

    private final ParticipantRepository participantRepository;

    AuthorProfilePage(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @GET
    public AuthorProfilePageView getAuthorProfilePage(@PathParam("author") int authorId) {
        Author author = participantRepository.getById(authorId)
                .filter(participant -> participant.isAnAuthor(Optional.empty()))
                .map(Author::new)
                .orElseThrow(NotFoundException::new);

        return new AuthorProfilePageView(author.getName());
    }

}
