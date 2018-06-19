package com.ssrn.fake_old_platform;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/login/pubSignInJoin.cfm")
@Produces(MediaType.TEXT_HTML)
public class LoginPage {
    @GET
    public LoginPageView get() {
        return new LoginPageView();
    }

    @POST
    public Response login(@FormParam(value = "username") String username) {
        return Response.seeOther(URI.create(String.format("/UserHome.cfm?username=%s", username))).build();
    }
}
