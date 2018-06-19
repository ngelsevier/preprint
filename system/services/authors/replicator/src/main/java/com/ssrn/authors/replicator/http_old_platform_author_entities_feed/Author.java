package com.ssrn.authors.replicator.http_old_platform_author_entities_feed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    private final int id;
    private final String name;
    private final int version;

    @JsonCreator
    public Author(@JsonProperty(value = "id", required = true) int id,
                  @JsonProperty(value = "name", required = true) String name,
                  @JsonProperty(value = "version", required = true) int version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                '}';
    }

}
