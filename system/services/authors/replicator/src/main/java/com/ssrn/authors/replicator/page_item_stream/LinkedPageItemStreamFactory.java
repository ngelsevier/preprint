package com.ssrn.authors.replicator.page_item_stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LinkedPageItemStreamFactory<TPage, TPageItem> {
    private final Function<TPage, Stream<? extends TPageItem>> pageItemStreamGenerator;
    private final Predicate<TPage> finalPageCondition;
    private final Function<TPage, TPage> nextPageRetriever;

    public LinkedPageItemStreamFactory(Function<TPage, Stream<? extends TPageItem>> pageItemStreamGenerator, Predicate<TPage> finalPageCondition, Function<TPage, TPage> nextPageRetriever) {
        this.pageItemStreamGenerator = pageItemStreamGenerator;
        this.finalPageCondition = finalPageCondition;
        this.nextPageRetriever = nextPageRetriever;
    }

    public Stream<TPageItem> createPageItemStreamStartingFrom(TPage initialPage) {
        LinkedPagesIterator<TPage> linkedPagesIterator = new LinkedPagesIterator<>(initialPage, finalPageCondition, nextPageRetriever);
        Spliterator<TPage> pagesSpliterator = Spliterators.spliteratorUnknownSize(linkedPagesIterator, Spliterator.DISTINCT | Spliterator.ORDERED);
        Stream<TPage> pageStream = StreamSupport.stream(pagesSpliterator, false);
        Stream<TPageItem> pageItemStream = pageStream.flatMap(pageItemStreamGenerator);

        return pageItemStream;
    }
}
