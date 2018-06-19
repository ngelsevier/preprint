package com.ssrn.fake_old_platform;

import io.dropwizard.jersey.sessions.Session;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/UserHome.cfm")
@Produces(MediaType.TEXT_HTML)
public class UserHomePage {
    private final ParticipantRepository participantRepository;
    private final int UNKNOWN_USER = -1;
    private Integer currentLoggedInUser = UNKNOWN_USER;

    UserHomePage(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @GET
    public UserHomePageView get(@QueryParam(value = "username") String username, @Session HttpSession session) {
        if (!StringUtils.isEmpty(username)) {
            participantRepository.getByUsername(username)
                    .ifPresent(participant -> currentLoggedInUser = participant.getAccountId());
        }

        session.setAttribute("loggedIn", currentLoggedInUser != UNKNOWN_USER);

        return new UserHomePageView(currentLoggedInUser);
    }
}
