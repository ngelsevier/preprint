package com.ssrn.papers.postgres;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class PartitionedMd5Range {
    private static final BigInteger MD5_UPPERBOUND = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE);
    private final List<Md5BasedPartition> partitions;

    PartitionedMd5Range(int partitionCount) {
        this.partitions = createPartitions(partitionCount);
    }

    public List<Md5BasedPartition> getPartitions() {
        return partitions;
    }

    private static List<Md5BasedPartition> createPartitions(int partitionCount) {
        BigInteger partitionWidth = MD5_UPPERBOUND.add(BigInteger.ONE).divide(BigInteger.valueOf(partitionCount));

        return IntStream.range(0, partitionCount).mapToObj(partitionIndex -> {
            BigInteger lowerBound = partitionWidth.multiply(BigInteger.valueOf(partitionIndex));
            BigInteger upperBound = partitionWidth.multiply(BigInteger.valueOf(partitionIndex + 1)).subtract(BigInteger.ONE);
            return new Md5BasedPartition(partitionIndex, lowerBound, upperBound);
        }).collect(Collectors.toList());
    }
}
