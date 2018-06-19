package com.ssrn.authors.replicator.http_old_platform_author_entities_feed;

import com.ssrn.authors.domain.Author;
import com.ssrn.authors.replicator.OldPlatformAuthorsStreamSource;
import com.ssrn.authors.replicator.page_item_stream.LinkedPageItemStreamFactory;

import javax.ws.rs.client.Client;
import java.util.logging.Level;
import java.util.stream.Stream;

public class AuthorsStreamSource implements OldPlatformAuthorsStreamSource {

    private final LinkedPageItemStreamFactory<Page, Author> linkedPageItemStreamFactory;
    private PageSource pageSource;

    public AuthorsStreamSource(String baseUrl, String basicAuthUsername, String basicAuthPassword, Client httpClient, int maxPageRequestRetries, Level httpRequestLogLevel) {
        pageSource = new PageSource(httpClient, baseUrl, basicAuthUsername, basicAuthPassword, maxPageRequestRetries, httpRequestLogLevel);
        linkedPageItemStreamFactory = new LinkedPageItemStreamFactory<>(
                page -> page.getAuthors().stream().map(oldPlatformAuthor -> new Author(
                        Integer.toString(oldPlatformAuthor.getId()),
                        oldPlatformAuthor.getVersion(), oldPlatformAuthor.getName(), false)),
                page -> false,
                page -> {
                    int paperCount = page.getAuthors().size();
                    return paperCount == 0 ? pageSource.getEntrypointPage() : pageSource.getPageStartingAfterAuthorId(page.getAuthors().get(paperCount - 1).getId());
                }
        );
    }

    @Override
    public Stream<com.ssrn.authors.domain.Author> getAuthorsStream() {
        Page entrypointPage = pageSource.getEntrypointPage();
        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(entrypointPage);
    }

    @Override
    public Stream<Author> getAuthorsStreamAfterId(String id) {
        Page entrypointPage = pageSource.getPageStartingAfterAuthorId(Integer.parseInt(id));
        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(entrypointPage);
    }

}
