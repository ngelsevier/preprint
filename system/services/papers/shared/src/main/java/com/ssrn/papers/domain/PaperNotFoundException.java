package com.ssrn.papers.domain;

public class PaperNotFoundException extends RuntimeException {
    private String paperId;

    public PaperNotFoundException(String paperId) {
        this.paperId = paperId;
    }

    public String getPaperId() {
        return paperId;
    }
}
