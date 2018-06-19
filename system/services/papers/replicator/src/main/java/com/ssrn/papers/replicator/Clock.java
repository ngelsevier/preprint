package com.ssrn.papers.replicator;

import org.joda.time.DateTime;

public interface Clock {
    DateTime getCurrentTime();
}
