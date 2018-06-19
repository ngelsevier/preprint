package com.ssrn.papers.replicator;

public class RequestRetryLimitedExceeded extends RuntimeException {
    public RequestRetryLimitedExceeded(Throwable cause) {
        super(cause);
    }
}
