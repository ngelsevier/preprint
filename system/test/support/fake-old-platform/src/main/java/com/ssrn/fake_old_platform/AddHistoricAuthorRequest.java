package com.ssrn.fake_old_platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddHistoricAuthorRequest {
    private final String name;

    @JsonCreator
    AddHistoricAuthorRequest(@JsonProperty(value = "name", required = true) String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
