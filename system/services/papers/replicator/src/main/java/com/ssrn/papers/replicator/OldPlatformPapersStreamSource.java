package com.ssrn.papers.replicator;

import com.ssrn.papers.domain.Paper;

import java.util.stream.Stream;

public interface OldPlatformPapersStreamSource {
    Stream<Paper> getPapersStream();

    Stream<Paper> getPapersStreamAfterId(String id);
}
