package com.ssrn.fake_old_platform;

import io.dropwizard.jersey.sessions.Session;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/rest/user/whoami")
@Produces("application/json; charset=utf-8")
public class WhoAmIFeedResource {

    WhoAmIFeedResource() { }

    @GET
    public Response getWhoAmIFeed(@Session HttpSession session) {
        String loggedInUser = "{\"name\":\"Test Admin\",\"id\":2711519}";
        String whoAmI = "{}";
        if(session.getAttribute("loggedIn") != null) {
            whoAmI = loggedInUser;
        }
        return Response.ok().entity(whoAmI)
                .header("Access-Control-Allow-Origin", "http://frontend-website.internal-service")
                .header("Access-Control-Allow-Credentials","true")
                .build();
    }
}
