package com.ssrn.fake_old_platform;

import com.ssrn.test.support.golden_data.*;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class Service extends io.dropwizard.Application<Configuration> {

    public static final int ENTITIES_FEED_ITEMS_PER_PAGE = 3;
    public static final int PAPER_EVENTS_FEED_EVENTS_PER_PAGE = 3;
    public static final int AUTHOR_EVENTS_FEED_EVENTS_PER_PAGE = 3;
    public static final String BASE_URL = "http://localhq.ssrn.com";

    public static final String PAPER_EVENTS_FEED_OLDEST_PAGE_ID = "00000-00000-00000-00000-00000";
    public static final String AUTHOR_EVENTS_FEED_OLDEST_PAGE_ID = "00000-00000-00000-00000-00000";
    public static final String BASIC_AUTH_PASSWORD = "password";
    private static final int MINIMUM_PAPER_EVENT_FEED_PAGE_COUNT = 3;
    private static final int MINIMUM_AUTHOR_EVENT_FEED_PAGE_COUNT = 3;
    public static final String BASIC_AUTH_USERNAME = "username";

    private Queue<ResponseDelay> responseDelays = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
        EventLog paperEventLog = new EventLog(PAPER_EVENTS_FEED_EVENTS_PER_PAGE, PAPER_EVENTS_FEED_OLDEST_PAGE_ID);
        EventLog authorEventLog = new EventLog(AUTHOR_EVENTS_FEED_EVENTS_PER_PAGE, AUTHOR_EVENTS_FEED_OLDEST_PAGE_ID);
        ParticipantRepository participantRepository = new ParticipantRepository();
        PaperRepository paperRepository = new PaperRepository(paperEventLog);
        SequentialIdGenerator participantSequentialIdGenerator = new SequentialIdGenerator(2000000000);

        DateTime eventTime = new DateTime(2017, 1, 1, 0, 0, DateTimeZone.UTC);

        SsrnUsers.all()
                .map(ssrnUser -> new Participant(ssrnUser.getUsername(), Integer.parseInt(ssrnUser.getId()), ssrnUser.getPublicDisplayName(), authorEventLog, paperRepository))
                .forEach(participantRepository::save);

        for (SsrnPaper ssrnPaper : SsrnPapers.all().collect(Collectors.toList())) {
            eventTime = eventTime.plusSeconds(1);

            Paper paper = new Paper(parseInt(ssrnPaper.getId()), ssrnPaper.getTitle(), ssrnPaper.getKeywords(), true, eventTime, false, false, false, false, Arrays.stream(ssrnPaper.getAuthors()).mapToInt(ssrnUser -> Integer.parseInt(ssrnUser.getId())).toArray(), ssrnPaper.getSubmissionStage());
            paperRepository.save(paper);

            for (SsrnUser author : ssrnPaper.getAuthors()) {
                eventTime = eventTime.plusSeconds(1);

                int authorId = Integer.parseInt(author.getId());
                Participant participant = participantRepository.getById(authorId).orElseGet(() -> {
                    throw new RuntimeException(String.format("No participant found with ID %s", authorId));
                });

                participant.addToPaper(parseInt(ssrnPaper.getId()), eventTime, true);
            }
        }

        SequentialIdGenerator paperSequentialAbstractIdGenerator = new SequentialIdGenerator(30000000);

        int nextAbstractId = paperSequentialAbstractIdGenerator.getNextId();
        Participant participantForNameChange = participantRepository.getById(Integer.parseInt(FakeSsrnUsers.CHANGE_ME.getId())).get();
        eventTime = eventTime.plusSeconds(1);
        paperRepository.save(new Paper(nextAbstractId, Integer.toString(nextAbstractId), null, false, eventTime, false, false, false, false, new int[]{participantForNameChange.getAccountId()}, "IN DRAFT"));
        eventTime = eventTime.plusSeconds(1);
        participantForNameChange.addToPaper(nextAbstractId, eventTime, true);

        for (int i = 0; i < Math.max(PAPER_EVENTS_FEED_EVENTS_PER_PAGE * MINIMUM_PAPER_EVENT_FEED_PAGE_COUNT, AUTHOR_EVENTS_FEED_EVENTS_PER_PAGE * MINIMUM_AUTHOR_EVENT_FEED_PAGE_COUNT); i++) {
            Integer participantId = participantSequentialIdGenerator.getNextId();
            eventTime = eventTime.plusSeconds(1);
            Participant participant = new Participant("default@email.com", participantId, "Default Name", authorEventLog, paperRepository);
            participantRepository.save(participant);

            Integer abstractId = paperSequentialAbstractIdGenerator.getNextId();
            eventTime = eventTime.plusSeconds(1);
            Paper paper = new Paper(abstractId, Integer.toString(abstractId), null, false, eventTime,
                    false, false, false, false,
                    new int[]{
                            Integer.parseInt(RealSsrnUsers.USER_1.getId()),
                            Integer.parseInt(RealSsrnUsers.USER_2.getId())
                    }, "IN DRAFT");
            paperRepository.save(paper);
            eventTime = eventTime.plusSeconds(1);
            participant.addToPaper(abstractId, eventTime, false);
            paper.addAuthor(participantId.toString(), eventTime, false);
            paperRepository.save(paper);
        }

        paperSequentialAbstractIdGenerator = new SequentialIdGenerator(40000000);

        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(Service::getUserByCredentials)
                .setRealm("SSRN REST API")
                .buildAuthFilter()));

        AuthorEntitiesFeedService authorEntitiesFeedService = new AuthorEntitiesFeedService(participantRepository);
        PaperEntitiesFeedService paperEntitiesFeedService = new PaperEntitiesFeedService(paperRepository);
        Queue<OverriddenResponse> overriddenResponses = new ConcurrentLinkedQueue<>();

        environment.jersey().register(new LoginPage());
        environment.jersey().register(new UserHomePage(participantRepository));
        environment.jersey().register(new SimpleSubmissionPage(
                paperSequentialAbstractIdGenerator, paperRepository, FakeSsrnUsers.JOHN_DOE.getId(), FakeSsrnUsers.JAMES_BROWN.getId(),
                FakeSsrnUsers.JOE_BLACK.getId(), FakeSsrnUsers.TIM_DUNCAN.getId(), participantRepository)
        );
        environment.jersey().register(new MyPapersPage(paperRepository));
        environment.jersey().register(new PaperEntitiesFeedResource(paperEntitiesFeedService));
        environment.jersey().register(new AuthorEntitiesFeedResource(authorEntitiesFeedService));
        environment.jersey().register(new PaperEventsFeedResource(paperEventLog));
        environment.jersey().register(new AuthorEventsFeedResource(authorEventLog));
        environment.jersey().register(new HealthcheckResource());
        environment.jersey().register(new MetadataResource(
                responseDelays, paperSequentialAbstractIdGenerator, paperRepository, participantSequentialIdGenerator,
                paperEventLog, paperEntitiesFeedService, authorEntitiesFeedService, overriddenResponses, authorEventLog, participantRepository)
        );
        environment.jersey().register(new ArticlePage(paperRepository));
        environment.jersey().register(new AuthorProfilePage(participantRepository));
        environment.jersey().register(new AuthorImage());
        environment.jersey().register(new PersonalInformationPage(participantRepository));
        environment.jersey().register(new AuthorPaperDisaocciationResource(paperRepository, participantRepository));
        environment.jersey().register(new StageOneClassificationPage(paperRepository, participantRepository));
        environment.jersey().register(new WhoAmIFeedResource());
        environment.jersey().register(new ModifySubmissionPopup(paperRepository, participantRepository));
        environment.jersey().register(new RevisionQueuePage(paperRepository));
        environment.jersey().register(new ReviseDetailPage(paperRepository));

        environment.servlets().addFilter("NextRequestDelayFilter", new NextRequestDelayFilter(responseDelays))
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/rest/papers/events/*",
                        "/rest/papers", "/rest/authors", "/rest/authors/events/*");
        environment.servlets().addFilter("ResponseOverrideFilter", new ResponseOverrideFilter(overriddenResponses))
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/rest/papers/events/*",
                        "/rest/papers", "/rest/authors", "/rest/authors/events/*");

        environment.servlets().setSessionHandler(new SessionHandler());
    }

    private static Optional<User> getUserByCredentials(BasicCredentials credentials) {
        return BASIC_AUTH_USERNAME.equals(credentials.getUsername()) && BASIC_AUTH_PASSWORD.equals(credentials.getPassword()) ?
                Optional.of(new User(credentials.getUsername())) :
                Optional.empty();
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new AssetsBundle("/assets/images", "/Images", null, "images"));
    }
}
