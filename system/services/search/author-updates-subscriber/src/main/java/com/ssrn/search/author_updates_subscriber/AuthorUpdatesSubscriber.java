package com.ssrn.search.author_updates_subscriber;

import com.ssrn.search.shared.AuthorRegistrar;
import com.ssrn.search.shared.AuthorRegistry;
import com.ssrn.search.shared.Librarian;
import com.ssrn.search.shared.Library;

class AuthorUpdatesSubscriber {
    private final StaleUpdatesDiscardingAuthorUpdatesListener authorUpdatesListener;

    AuthorUpdatesSubscriber(Library library, AuthorRegistry authorRegistry) {
        AuthorRegistrar authorRegistrar = new AuthorRegistrar(authorRegistry);
        Librarian librarian = new Librarian(library, authorRegistry);

        this.authorUpdatesListener = new StaleUpdatesDiscardingAuthorUpdatesListener(authorUpdates -> {
            authorRegistrar.updateRegistry(authorUpdates);
            librarian.applyAuthorUpdates(authorUpdates);
        });
    }

    void subscribeTo(AuthorUpdatesStream stream) {
        stream.onAuthorUpdatesReceived(authorUpdatesListener::notify);
    }
}
