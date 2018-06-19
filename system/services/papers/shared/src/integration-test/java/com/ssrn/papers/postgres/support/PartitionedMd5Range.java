package com.ssrn.papers.postgres.support;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ssrn.papers.postgres.support.Md5BasedPartition.md5BasedPartition;

public class PartitionedMd5Range {
    private final List<Md5BasedPartition> md5BasedPartitions;

    public static PartitionedMd5Range partitionedMd5Range(int partitionCount) {
        return new PartitionedMd5Range(partitionCount);
    }

    private PartitionedMd5Range(int totalPartitions) {
        this.md5BasedPartitions = IntStream.range(0, totalPartitions)
                .mapToObj(i -> md5BasedPartition(i, totalPartitions))
                .collect(Collectors.toList());
    }

    public String getRandomStringInPartition(int partitionIndex) {
        return md5BasedPartitions.get(partitionIndex).getRandomStringInPartition();
    }
}
