package com.ssrn.search.shared.matchers.mockito;

import org.mockito.ArgumentMatcher;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.argThat;

public class CollectionContainingOnlyMatcher<T> implements ArgumentMatcher<Collection<T>> {
    private final T[] expectedItems;

    public static <T> Collection<T> collectionContainingOnly(T... expectedItems) {
        return argThat(new CollectionContainingOnlyMatcher<>(expectedItems));
    }

    private CollectionContainingOnlyMatcher(T[] expectedItems) {
        this.expectedItems = expectedItems;
    }

    @Override
    public boolean matches(Collection<T> argument) {
        return argument.size() == expectedItems.length && argument.containsAll(asList(expectedItems));
    }
}
