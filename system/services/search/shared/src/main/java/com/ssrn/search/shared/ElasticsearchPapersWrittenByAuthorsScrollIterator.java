package com.ssrn.search.shared;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.entity.ContentType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

class ElasticsearchPapersWrittenByAuthorsScrollIterator implements Iterator<SearchIndexPaper[]> {
    private static final String SCROLL_TIMEOUT = "1m";

    private final String indexName;
    private final String documentType;
    private final ElasticsearchClient elasticsearchClient;
    private final int scrollSize;
    private String scrollId;
    private String[] authorIds;
    private int lastResponsePaperCount;

    ElasticsearchPapersWrittenByAuthorsScrollIterator(String[] authorIds, String indexName, String documentType, ElasticsearchClient elasticsearchClient, int scrollSize) {
        this.authorIds = authorIds;
        this.indexName = indexName;
        this.documentType = documentType;
        this.elasticsearchClient = elasticsearchClient;
        this.scrollSize = scrollSize;
    }

    @Override
    public boolean hasNext() {
        if ((scrollId == null) || (lastResponsePaperCount > 0)) {
            return true;
        }

        elasticsearchClient.makeRequest("DELETE", String.format("/_search/scroll/%s", scrollId), ContentType.TEXT_PLAIN, "", 200);
        return false;
    }

    @Override
    public SearchIndexPaper[] next() {
        ElasticsearchSearchHitsSource[] searchHits = getNextPageOfSearchHits();

        SearchIndexPaper[] searchIndexPapers = Arrays.stream(searchHits)
                .map(ElasticsearchPapersWrittenByAuthorsScrollIterator::createSearchIndexPaper)
                .toArray(SearchIndexPaper[]::new);

        lastResponsePaperCount = searchIndexPapers.length;

        return searchIndexPapers;
    }

    private static SearchIndexPaper createSearchIndexPaper(ElasticsearchSearchHitsSource searchHit) {
        return new SearchIndexPaper(searchHit.getId(), searchHit.getTitle(), searchHit.getKeywords(), searchHit.getAuthors());
    }

    private ElasticsearchSearchHitsSource[] getNextPageOfSearchHits() {
        String responseBody = executeNextScrollQuery();
        return JsonPath.parse(responseBody).read("$.hits.hits[*]._source", ElasticsearchSearchHitsSource[].class);
    }

    private String executeNextScrollQuery() {
        if (scrollId != null) {
            return elasticsearchClient.makeRequest("POST", "/_search/scroll", createSubsequentScrolledPageRequestQuery(scrollId), 200).getBody();
        }

        String responseBody = elasticsearchClient.makeRequest("POST", String.format("/%s/%s/_search?scroll=%s", indexName, documentType, SCROLL_TIMEOUT), createFirstScrollPageRequestQuery(scrollSize, authorIds), 200).getBody();
        scrollId = parseScrollIdFrom(responseBody);
        return responseBody;

    }

    private static HashMap<String, Object> createFirstScrollPageRequestQuery(final int scrollSize, String[] authorIds) {
        return new HashMap<String, Object>() {
            {
                put("size", scrollSize);
                put("sort", new String[]{"_doc"});
                put("query", new HashMap<String, Object>() {
                    {
                        put("constant_score", new HashMap<String, Object>() {
                            {
                                put("filter", new HashMap<String, Object>() {
                                    {
                                        put("terms", new HashMap<String, Object>() {
                                            {
                                                put("authors.id", authorIds);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

    }

    private static HashMap<String, Object> createSubsequentScrolledPageRequestQuery(String scrollId) {
        return new HashMap<String, Object>() {{
            put("scroll", SCROLL_TIMEOUT);
            put("scroll_id", scrollId);
        }};
    }

    private static String parseScrollIdFrom(String responseBody) {
        return JsonPath.parse(responseBody).read("$._scroll_id", String.class);
    }

}
