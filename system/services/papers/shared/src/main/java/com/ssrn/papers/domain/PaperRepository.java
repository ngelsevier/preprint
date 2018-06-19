package com.ssrn.papers.domain;

import java.util.List;

public interface PaperRepository extends AutoCloseable {
    void save(Paper paper);

    void save(List<Paper> papers);

    Paper getById(String id);

    boolean hasPaper(String id);
}
