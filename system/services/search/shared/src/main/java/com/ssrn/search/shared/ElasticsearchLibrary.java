package com.ssrn.search.shared;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.ssrn.search.domain.AuthorUpdate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ElasticsearchLibrary implements Library {
    private static final String DOCUMENT_TYPE = "paper";
    private final String papersIndexName;
    private int scrollSize;
    private final String authorsIndexName;

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

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchLibrary(String papersIndexName, String authorsIndexName, String nodeHostName, int nodePort, int scrollSize) {
        this.papersIndexName = papersIndexName;
        this.scrollSize = scrollSize;
        this.authorsIndexName = authorsIndexName;
        elasticsearchClient = new ElasticsearchClient(nodeHostName, nodePort);
    }

    @Override
    public SearchResults searchForItemsMatching(String searchTerm, int from, int size) {
        try {
            ElasticsearchClient.Response response = elasticsearchClient.makeRequest(
                    "GET",
                    String.format("/%s,%s/_search?search_type=dfs_query_then_fetch&q=%s&from=%d&size=%d", papersIndexName, authorsIndexName, URLEncoder.encode(searchTerm, "UTF-8"), from, size),
                    requestEntity(),
                    200);

            ElasticsearchSearchHit elasticsearchSearchHit = parseSearchHitsFromResponse(response.getBody());
            BaseSearchResult[] baseSearchResults = Arrays.stream(elasticsearchSearchHit.getElasticsearchSearchHitDetails())
                    .map(searchHit -> (searchHit.getSource().getName() != null) ?
                            new AuthorSearchResult(searchHit.getSource().getId(), searchHit.getSource().getName(), searchHit.getHighlights()) :
                            new PaperSearchResult(
                                    searchHit.getSource().getId(),
                                    searchHit.getSource().getTitle(),
                                    searchHit.getSource().getKeywords(),
                                    Arrays.stream(searchHit.getSource().getAuthors())
                                            .map(paperAuthor -> new SearchResultAuthor(paperAuthor.getId(), paperAuthor.getName(), searchHit.getHighlights()))
                                            .toArray(SearchResultAuthor[]::new),
                                    searchHit.getHighlights())
                    )
                    .toArray(BaseSearchResult[]::new);

            return new SearchResults(elasticsearchSearchHit.getTotal(), baseSearchResults);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void update(List<SearchIndexPaper> papers) {
        if (papers.size() > 0) {
            elasticsearchClient.makeBulkIndexRequest("POST", String.format("/%s/%s/_bulk", papersIndexName, DOCUMENT_TYPE), papers);
        }
    }

    @Override
    public void delete(List<String> paperIds) {
        if (paperIds.size() > 0) {
            elasticsearchClient.makeBulkDeleteRequest("POST", String.format("/%s/%s/_bulk", papersIndexName, DOCUMENT_TYPE), paperIds);
        }
    }

    @Override
    public Stream<SearchIndexPaper> getPapersWrittenBy(Collection<AuthorUpdate.Author> authors) {
        String[] authorIds = authors.stream().map(AuthorUpdate.Author::getId).toArray(String[]::new);
        Spliterator<SearchIndexPaper[]> spliterator = Spliterators.spliteratorUnknownSize(new ElasticsearchPapersWrittenByAuthorsScrollIterator(authorIds, papersIndexName, DOCUMENT_TYPE, elasticsearchClient, scrollSize), Spliterator.DISTINCT);
        return StreamSupport.stream(spliterator, false).flatMap(Arrays::stream);
    }

    private static Map<String, Object> requestEntity() {
        return new HashMap<String, Object>() {{
            put("highlight", new HashMap<String, Object>() {{
                put("type", "plain");
                put("require_field_match", false);
                put("fields", new HashMap<String, Object>() {{
                    put("title", new HashMap<>());
                    put("keywords", new HashMap<>());
                    put("authors.name", new HashMap<>());
                    put("name", new HashMap<>());
                }});
            }});
        }};
    }

    private static ElasticsearchSearchHit parseSearchHitsFromResponse(String responseBody) {
        return JsonPath.parse(responseBody).read("$.hits", ElasticsearchSearchHit.class);
    }


}
