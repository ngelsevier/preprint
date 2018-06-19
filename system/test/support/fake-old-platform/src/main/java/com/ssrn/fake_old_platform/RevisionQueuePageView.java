package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

import java.util.List;
import java.util.stream.Collectors;

public class RevisionQueuePageView  extends View {
    private final PaperRepository paperRepository;

    protected RevisionQueuePageView(PaperRepository paperRepository) {
        super("revision-queue.mustache");
        this.paperRepository = paperRepository;
    }

    public List<Paper> getPapers() {
        return paperRepository.getAll().collect(Collectors.toList());
    }
}
