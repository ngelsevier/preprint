package com.ssrn.authors.updates_publisher;

import com.ssrn.authors.domain.AuthorUpdate;

public interface AuthorUpdatesStream {
    void publish(AuthorUpdate authorUpdate);
}
