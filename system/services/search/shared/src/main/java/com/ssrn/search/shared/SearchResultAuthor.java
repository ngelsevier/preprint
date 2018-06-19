package com.ssrn.search.shared;

import java.util.Arrays;
import java.util.Map;

public class SearchResultAuthor {
    private final String id;
    private final String name;
    private Map<String, String[]> highlights;

    public SearchResultAuthor(String id, String name, Map<String, String[]> highlights) {
        this.id = id;
        this.name = name;
        this.highlights = highlights;
    }

    public String getName() {
        String[] highlightedAuthorsNames = highlights.get("authors.name");
        if (highlightedAuthorsNames != null) {
            String[] matchedName = Arrays.stream(highlightedAuthorsNames)
                    .filter(s -> s.replaceAll("<em>|</em>", "").equals(name)).toArray(String[]::new);
            return matchedName.length > 0 ? matchedName[0] : name;
        }
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SearchResultAuthor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
