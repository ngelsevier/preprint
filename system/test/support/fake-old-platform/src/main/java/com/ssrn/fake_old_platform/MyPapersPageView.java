package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MyPapersPageView extends View {

    private final List<PaperView> papers;

    MyPapersPageView(List<PaperView> papers) {
        super("my-papers-page.mustache");
        this.papers = papers;
    }

    public List<PaperView> getAbstracts() {
        return papers.stream().filter(p -> p.getSubmissionStage().compareTo("APPROVED") != 0).collect(Collectors.toList());
    }

    public List<PaperView> getPubliclyAvailablePapers() {
        return papers.stream().filter(p -> p.getSubmissionStage().equals("APPROVED")).collect(Collectors.toList());
    }

    public static class PaperView {
        private final Paper paper;
        private final Function<Paper, String> submissionPageUrlGenerator;

        PaperView(Paper paper, Function<Paper, String> submissionPageUrlGenerator) {
            this.paper = paper;
            this.submissionPageUrlGenerator = submissionPageUrlGenerator;
        }

        public int getId() {
            return paper.getId();
        }

        public String getSubmissionPageUrl() {
            return submissionPageUrlGenerator.apply(paper);
        }

        public String getTitle() {
            return paper.getTitle();
        }

        public String getSubmissionStage() {
            return paper.getSubmissionStage();
        }

    }
}
