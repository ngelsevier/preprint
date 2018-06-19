package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.TEXT_HTML)
@Path("/Participant.cfm")
public class PersonalInformationPage {
    private final ParticipantRepository participantRepository;

    PersonalInformationPage(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @GET
    public PersonalInformationPageView getPage(@QueryParam(value = "partid") Integer partId) {
        return new PersonalInformationPageView(partId);
    }

    @POST
    public View updateDisplayName(@FormParam(value = "partId") Integer partId, @FormParam(value = "txtPrefFirstName") String firstName, @FormParam(value = "txtPrefLastName") String lastName) {
        Participant participant = participantRepository.getById(partId).get();
        participant.setName(firstName, lastName, null);

        return new PersonalInformationPageView(partId);
    }
}
