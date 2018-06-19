package com.ssrn.papers.emitter;

import com.ssrn.papers.domain.Paper;

public interface PapersSink {
    void streamPaper(Paper paper);
}
