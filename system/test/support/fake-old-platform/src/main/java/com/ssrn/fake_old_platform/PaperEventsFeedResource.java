package com.ssrn.fake_old_platform;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@PermitAll
@Path("/rest/papers/events")
@Produces("application/json; charset=utf-8")
public class PaperEventsFeedResource {

    private final EventLog eventLog;

    PaperEventsFeedResource(EventLog eventLog) {
        this.eventLog = eventLog;
    }

    @GET
    public Response getEntrypointUrlWorkingPage(@Context UriInfo uriInfo, @Context Request request) {
        EntityTag etag = generateETagForEventPage(eventLog.getNewestPage());

        return generateResponse(etag, eventLog.getNewestPage(), uriInfo, request);
    }

    @GET
    @Path("/{pageId}")
    public Response getCanonicalUrlWorkingPage(@Context UriInfo uriInfo, @PathParam("pageId") String pageId, @Context Request request) {
        EventLogPage page = eventLog.getPage(pageId);
        if (page == null) {
            throw new NotFoundException();
        }

        EntityTag etag = generateETagForEventPage(page);

        return generateResponse(etag, page, uriInfo, request);
    }

    private EntityTag generateETagForEventPage(EventLogPage page) {
        int pageUniqueHashCode = page.getEvents().stream()
                .map(Event::getEntityId)
                .reduce((o, o2) -> o + o2).get().hashCode();

        return new EntityTag(String.valueOf(pageUniqueHashCode));
    }

    private Response generateResponse(EntityTag etag, EventLogPage page, @Context UriInfo uriInfo, @Context Request request) {
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);

        if (builder == null) {
            builder = Response.ok(new HalEventsPage(page, uriInfo.getRequestUri().toString(), "http://localhq.ssrn.com/rest/papers/events"))
                    .header("Cache-Control", page.getNextPageId() == null ? "no-cache" : "max-age=3600");
            builder.tag(etag);
        }

        return builder.build();
    }
}
