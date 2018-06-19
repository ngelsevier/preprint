package com.ssrn.authors.replicator;

import com.ssrn.authors.replicator.concurrency.SingleJobRunner;
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
    private final AuthorEventsProjector authorEventsProjector;
    private final AuthorReplicator authorReplicator;
    private final SingleJobRunner authorEventsReplicationSinglejobRunner;
    private final SingleJobRunner authorReplicationSinglejobRunner;

    JobsResource(AuthorEventsProjector authorEventsProjector, AuthorReplicator authorReplicator,
                 SingleJobRunner authorEventsReplicationSinglejobRunner, SingleJobRunner authorReplicationSinglejobRunner) {
        this.authorEventsProjector = authorEventsProjector;
        this.authorReplicator = authorReplicator;
        this.authorEventsReplicationSinglejobRunner = authorEventsReplicationSinglejobRunner;
        this.authorReplicationSinglejobRunner = authorReplicationSinglejobRunner;
    }

    @Path("/event-replication")
    @POST
    public Response createEventReplicationJob() {
        return handleRequestToCreateInstanceOfJob("event replication", authorEventsProjector::applyNewEvents, authorEventsReplicationSinglejobRunner);
    }

    @Path("/entity-replication")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAuthorReplicationJob(AuthorReplicationJobConfiguration configuration) {
        return handleRequestToCreateInstanceOfJob("entity replication", () ->
                authorReplicator.replicateAuthors(configuration.getJobBatchSize(), configuration.getDatabaseUpsertBatchSize()), authorReplicationSinglejobRunner);
    }

    private static Response handleRequestToCreateInstanceOfJob(String jobName, ThrowingRunnable throwingRunnable, SingleJobRunner singleJobRunner) {
        boolean accepted = singleJobRunner.run(throwingRunnable, e -> LOGGER.error(
                String.format("An exception was thrown whilst running the %s job %s", jobName, e.getMessage()),
                e));

        return Response.status(accepted ? Response.Status.CREATED : Response.Status.CONFLICT).build();
    }

}
