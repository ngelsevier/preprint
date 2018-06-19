package com.ssrn.search.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorUpdate {
    private Author author;
    private String id;

    @JsonCreator
    public AuthorUpdate(@JsonProperty(value = "id", required = true) String id,
                        @JsonProperty(value = "author", required = true) Author author) {
        this.id = id;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public Author getAuthor() {
        return author;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {

        private final String id;
        private final String name;
        private boolean removed;

        @JsonCreator
        public Author(@JsonProperty(value = "id", required = true) String id,
                      @JsonProperty(value = "name", required = true) String name,
                      @JsonProperty(value = "removed", required = true) boolean removed) {
            this.id = id;
            this.name = name;
            this.removed = removed;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public boolean isRemoved() {
            return removed;
        }

        @Override
        public String toString() {
            return "Author{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", removed=" + removed +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AuthorUpdate{" +
                "author=" + author +
                ", id='" + id + '\'' +
                '}';
    }

}
