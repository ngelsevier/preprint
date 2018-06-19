package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.domain.AuthorUpdate;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StaleUpdatesDiscardingAuthorUpdatesListener {
    private final Consumer<List<AuthorUpdate>> delegateConsumer;

    StaleUpdatesDiscardingAuthorUpdatesListener(Consumer<List<AuthorUpdate>> delegateConsumer) {
        this.delegateConsumer = delegateConsumer;
    }

    public void notify(List<AuthorUpdate> authorUpdates) {
        ArrayList<AuthorUpdate> authorUpdatesLatestFirst = reverse(authorUpdates);

        List<AuthorUpdate> freshAuthorUpdates = authorUpdatesLatestFirst.stream()
                .filter(authorNotSeenBefore())
                .collect(Collectors.toList());

        Collections.reverse(freshAuthorUpdates);

        delegateConsumer.accept(freshAuthorUpdates);
    }

    private static ArrayList<AuthorUpdate> reverse(List<AuthorUpdate> authorUpdates) {
        ArrayList<AuthorUpdate> copyOfList = new ArrayList<>(authorUpdates);
        Collections.reverse(copyOfList);
        return copyOfList;
    }

    private static Predicate<AuthorUpdate> authorNotSeenBefore() {
        Set<String> authorIds = new HashSet<>();
        return authorUpdate -> authorIds.add(authorUpdate.getId());
    }

}
