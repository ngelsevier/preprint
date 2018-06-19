package com.ssrn.authors.replicator.page_item_stream;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class LinkedPagesIterator<T> implements Iterator<T> {
    private T previousPage;
    private final Predicate<T> finalPageCondition;
    private final Function<T, T> nextPageRetriever;
    private Boolean initialPagePending = true;

    LinkedPagesIterator(T initialPage, Predicate<T> finalPageCondition, Function<T, T> nextPageRetriever) {
        this.previousPage = initialPage;
        this.finalPageCondition = finalPageCondition;
        this.nextPageRetriever = nextPageRetriever;
    }

    @Override
    public boolean hasNext() {
        return initialPagePending || !finalPageCondition.test(previousPage);
    }

    @Override
    public T next() {
        if (initialPagePending) {
            initialPagePending = false;
            return previousPage;
        }

        T page = nextPageRetriever.apply(previousPage);
        previousPage = page;
        return page;
    }
}
