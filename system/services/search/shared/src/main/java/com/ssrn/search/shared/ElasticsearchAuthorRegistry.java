package com.ssrn.search.shared;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.ssrn.search.domain.AuthorUpdate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ElasticsearchAuthorRegistry implements AuthorRegistry {
    private static final String DOCUMENT_TYPE = "author";

    private final ElasticsearchClient elasticsearchClient;
    private final String indexName;

    static {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JacksonMappingProvider jacksonMappingProvider = new JacksonMappingProvider();
            private JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jacksonJsonProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return jacksonMappingProvider;
            }
        });
    }

    public ElasticsearchAuthorRegistry(String indexName, String nodeHostName, int nodePort) {
        this.indexName = indexName;
        elasticsearchClient = new ElasticsearchClient(nodeHostName, nodePort);
    }

    @Override
    public AuthorUpdate.Author[] getByIds(Collection<String> authorIds) {
        ElasticsearchClient.Response response = elasticsearchClient.makeRequest("GET", String.format("/%s/%s/_mget", indexName, DOCUMENT_TYPE), createMultiGetRequestBody(authorIds), 200);

        ElasticsearchSearchAuthorHitsSource[] searchHits = JsonPath.parse(response.getBody()).read("$.docs[*]._source", ElasticsearchSearchAuthorHitsSource[].class);
        return Arrays.stream(searchHits)
                .map(searchHit -> new AuthorUpdate.Author(searchHit.getId(), searchHit.getName(), true))
                .toArray(AuthorUpdate.Author[]::new);
    }

    @Override
    public void update(List<AuthorUpdate.Author> authors) {
        List<SearchIndexAuthor> searchIndexAuthors = authors.stream()
                .map(author -> new SearchIndexAuthor(author.getId(), author.getName()))
                .collect(Collectors.toList());

        elasticsearchClient.makeBulkIndexRequest("POST", String.format("/%s/%s/_bulk", indexName, DOCUMENT_TYPE), searchIndexAuthors);
    }

    @Override
    public void delete(List<String> authorIds) {
        elasticsearchClient.makeBulkDeleteRequest("POST", String.format("/%s/%s/_bulk", indexName, DOCUMENT_TYPE), authorIds);
    }

    private static HashMap<String, Object> createMultiGetRequestBody(Collection<String> authorIds) {
        return new HashMap<String, Object>() {
            {
                put("ids", authorIds);
            }
        };
    }

}
