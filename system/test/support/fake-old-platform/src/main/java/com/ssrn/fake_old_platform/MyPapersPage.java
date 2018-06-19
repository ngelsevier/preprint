package com.ssrn.fake_old_platform;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
public class MyPapersPage {

    private final PaperRepository paperRepository;

    MyPapersPage(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    @GET
    @Path("/submissions/MyPapers.cfm")
    public MyPapersPageView getMyPapersPage() {

        List<MyPapersPageView.PaperView> paperViews = paperRepository.getAll()
                .map(paper -> new MyPapersPageView.PaperView(paper, MyPapersPage::submissionPageUrl))
                .collect(Collectors.toList());

        return new MyPapersPageView(paperViews);
    }

    private static String submissionPageUrl(Paper p) {
        return String.format("/submissions/SimpleSubmission.cfm?AbstractID=%s", p.getId());
    }
}
