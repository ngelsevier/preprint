package com.ssrn.fake_old_platform;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class RevisionQueuePage {

    private final PaperRepository paperRepository;

    RevisionQueuePage(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    @GET
    @Path("/submissions/Revisequeue.cfm")
    public RevisionQueuePageView getRevisionQueuePage() {
        return new RevisionQueuePageView(paperRepository);
    }
}
