package com.ssrn.papers.replicator.http_old_platform_paper_entities_feed;

import com.ssrn.papers.domain.Paper;
import com.ssrn.papers.replicator.OldPlatformPapersStreamSource;
import com.ssrn.papers.replicator.page_item_stream.LinkedPageItemStreamFactory;

import javax.ws.rs.client.Client;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.ssrn.papers.domain.SubmissionStage.fromString;

public class PapersStreamSource implements OldPlatformPapersStreamSource {

    private final LinkedPageItemStreamFactory<Page, Paper> linkedPageItemStreamFactory;
    private PageSource pageSource;

    public PapersStreamSource(String baseUrl, String basicAuthUsername, String basicAuthPassword, Client httpClient, int maxPageRequestRetries, Level httpRequestLogLevel) {
        pageSource = new PageSource(httpClient, baseUrl, basicAuthUsername, basicAuthPassword, maxPageRequestRetries, httpRequestLogLevel);
        linkedPageItemStreamFactory = new LinkedPageItemStreamFactory<>(
                page -> page.getPapers().stream()
                        .map(oldPlatformPaper -> new Paper(
                                Integer.toString(oldPlatformPaper.getId()),
                                oldPlatformPaper.getVersion(),
                                oldPlatformPaper.getTitle(),
                                oldPlatformPaper.getKeywords(),
                                oldPlatformPaper.getAuthorIds(),
                                oldPlatformPaper.isPaperPrivate(),
                                oldPlatformPaper.isConsideredIrrelevant(),
                                oldPlatformPaper.isPaperRestricted(),
                                oldPlatformPaper.getSubmissionStage() != null ? fromString(oldPlatformPaper.getSubmissionStage()) : null)
                        ),
                page -> false,
                page -> {
                    int paperCount = page.getPapers().size();
                    return paperCount == 0 ? pageSource.getEntrypointPage() : pageSource.getPageStartingAfterPaperId(page.getPapers().get(paperCount - 1).getId());
                }
        );
    }

    @Override
    public Stream<com.ssrn.papers.domain.Paper> getPapersStream() {
        Page entrypointPage = pageSource.getEntrypointPage();
        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(entrypointPage);
    }

    @Override
    public Stream<Paper> getPapersStreamAfterId(String id) {
        Page entrypointPage = pageSource.getPageStartingAfterPaperId(Integer.parseInt(id));
        return linkedPageItemStreamFactory.createPageItemStreamStartingFrom(entrypointPage);
    }

}
