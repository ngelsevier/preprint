package com.ssrn.authors.replicator;

public class RequestRetryLimitedExceeded extends RuntimeException {
    public RequestRetryLimitedExceeded(Throwable cause) {
        super(cause);
    }
}
