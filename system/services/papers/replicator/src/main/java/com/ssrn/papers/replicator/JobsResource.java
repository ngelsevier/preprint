package com.ssrn.papers.replicator;

import com.ssrn.papers.replicator.concurrency.SingleJobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/jobs")
public class JobsResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobsResource.class.getName());

    private final PaperEventsReplicator paperEventsReplicator;
    private final PaperReplicator paperReplicator;
    private final SingleJobRunner eventReplicationSingleJobRunner;
    private final SingleJobRunner paperReplicationSinglejobRunner;

    JobsResource(PaperEventsReplicator paperEventsReplicator, PaperReplicator paperReplicator,
                 SingleJobRunner eventReplicationSingleJobRunner, SingleJobRunner singleJobRunner) {
        this.paperEventsReplicator = paperEventsReplicator;
        this.paperReplicator = paperReplicator;
        this.eventReplicationSingleJobRunner = eventReplicationSingleJobRunner;
        this.paperReplicationSinglejobRunner = singleJobRunner;
    }

    @Path("/event-replication")
    @POST
    public Response createEventReplicationJob() {
        return handleRequestToCreateInstanceOfJob("event replication", paperEventsReplicator::replicateNewEvents, eventReplicationSingleJobRunner);
    }

    @Path("/entity-replication")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPaperReplicationJob(PaperReplicationJobConfiguration configuration) {
        return handleRequestToCreateInstanceOfJob("entity replication", () -> {
            paperReplicator.replicatePapers(configuration.getJobBatchSize(), configuration.getDatabaseUpsertBatchSize());
        }, paperReplicationSinglejobRunner);
    }


    private static Response handleRequestToCreateInstanceOfJob(String jobName, ThrowingRunnable throwingRunnable, SingleJobRunner singleJobRunner) {
        boolean accepted = singleJobRunner.run(throwingRunnable, e -> LOGGER.error(
                String.format("An exception was thrown whilst running the %s job %s", jobName, e.getMessage()),
                e));

        return Response.status(accepted ? Response.Status.CREATED : Response.Status.CONFLICT).build();
    }
}
