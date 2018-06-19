package com.ssrn.authors.replicator.http_old_platform_author_events_feed;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

class Feed {
    private final PageSource pageSource;
    private final String entrypointUrl;

    Feed(PageSource pageSource, String baseUrl) {
        entrypointUrl = String.format("%s/rest/authors/events", baseUrl);
        this.pageSource = pageSource;
    }

    Page seekPageContainingEvent(String eventId) {
        return seekPageThatSatisfies(halEventsPage -> pageContainsEventWithId(eventId, halEventsPage));
    }

    Page seekOldestPageInFeed() {
        return seekPageThatSatisfies(halEventsPage -> halEventsPage.getLinks().getPreviousArchive() == null);
    }

    private Page seekPageThatSatisfies(Predicate<Page> soughtPageCondition) {
        NewestToOldestPageIterator newestToOldestPageIterator =
                new NewestToOldestPageIterator(entrypointUrl, pageSource, soughtPageCondition);
        Spliterator<Page> halEventsPageSpliterator =
                Spliterators.spliteratorUnknownSize(newestToOldestPageIterator, Spliterator.DISTINCT | Spliterator.ORDERED);

        return StreamSupport.stream(halEventsPageSpliterator, false)
                .reduce((a, b) -> b)
                .get();
    }

    private static boolean pageContainsEventWithId(String eventId, Page page) {
        return page.getEvents().stream()
                .anyMatch(httpOldPlatformPaperEvent -> eventId.equals(httpOldPlatformPaperEvent.getId()));
    }

    private static class NewestToOldestPageIterator implements Iterator<Page> {
        private final PageSource pageSource;
        private final Predicate<Page> soughtPageCondition;

        private String initialPageUrl;
        private Page currentPage;
        private boolean soughtPageAlreadyEncountered = false;

        NewestToOldestPageIterator(String initialPageUrl, PageSource pageSource, Predicate<Page> soughtPageCondition) {
            this.initialPageUrl = initialPageUrl;
            this.pageSource = pageSource;
            this.soughtPageCondition = soughtPageCondition;
        }

        @Override
        public boolean hasNext() {
            if (soughtPageAlreadyEncountered) {
                return false;
            }

            currentPage = retrieveAnotherPage();
            soughtPageAlreadyEncountered = soughtPageCondition.test(currentPage);
            return true;
        }

        private Page retrieveAnotherPage() {
            String pageUrl = currentPage == null ?
                    initialPageUrl :
                    currentPage.getLinks().getPreviousArchive().getHref();

            return pageSource.getPageAt(pageUrl);
        }

        @Override
        public Page next() {
            return currentPage;
        }

    }
}
