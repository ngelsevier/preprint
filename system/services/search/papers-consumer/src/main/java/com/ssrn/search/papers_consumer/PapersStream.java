package com.ssrn.search.papers_consumer;

import com.ssrn.search.domain.Paper;

import java.util.List;
import java.util.function.Consumer;

public interface PapersStream extends AutoCloseable {
    void onPapersReceived(Consumer<List<Paper>> papersConsumer);
}
