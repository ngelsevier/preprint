package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

import java.util.List;
import java.util.stream.Collectors;

public class ReviseDetailPageView extends View {

    private final int abstractId;

    protected ReviseDetailPageView(int abstractId) {
        super("revise-detail.mustache");
        this.abstractId = abstractId;
    }

    public int getAbstractId() {
        return abstractId;
    }
}
