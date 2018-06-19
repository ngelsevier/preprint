package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.domain.AuthorUpdate;

import java.util.List;
import java.util.function.Consumer;

public interface AuthorUpdatesStream extends AutoCloseable {
    void onAuthorUpdatesReceived(Consumer<List<AuthorUpdate>> authorsConsumer);
}
