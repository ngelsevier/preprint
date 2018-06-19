package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    private final Participant participant;

    Author(Participant participant) {
        this.participant = participant;
    }

    public int getId() {
        return participant.getAccountId();
    }

    public String getName() {
        return participant.getName();
    }

    public int getVersion() {
        return participant.getAuthorVersion();
    }
}
