package com.ssrn.papers.postgres.support;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ssrn.papers.postgres.support.PartitionedMd5Range.partitionedMd5Range;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PartitionedMd5RangeTest {

    private static final BigInteger MD5_UPPERBOUND = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE);

    @Test
    public void shouldGenerateRandomStringsThatBelongToSpecifiedMd5RangePartition() {
        int partitionCount = 4;

        List<AbstractMap.SimpleEntry<BigInteger, BigInteger>> partitions = createMd5BasedPartitions(partitionCount);

        PartitionedMd5Range partitionedMd5Range = partitionedMd5Range(partitionCount);

        int numberOfStringsToGeneratePerPartition = 10;

        IntStream.range(0, partitionCount)
                .forEach(partitionIndex -> {
                    IntStream.range(0, numberOfStringsToGeneratePerPartition)
                            .forEach(ignored -> assertThat(partitionedMd5Range.getRandomStringInPartition(partitionIndex), is(inPartition(partitions.get(partitionIndex)))));
                });
    }

    static List<AbstractMap.SimpleEntry<BigInteger, BigInteger>> createMd5BasedPartitions(int partitionCount) {
        BigInteger partitionWidth = MD5_UPPERBOUND.add(BigInteger.ONE).divide(BigInteger.valueOf(partitionCount));

        return IntStream.rangeClosed(1, partitionCount).mapToObj(i -> {
            BigInteger lowerBound = partitionWidth.multiply(BigInteger.valueOf(i - 1));
            BigInteger upperBound = partitionWidth.multiply(BigInteger.valueOf(i)).subtract(BigInteger.ONE);
            return new HashMap.SimpleEntry<>(lowerBound, upperBound);
        }).collect(Collectors.toList());
    }

    static Matcher<String> inPartition(AbstractMap.SimpleEntry<BigInteger, BigInteger> expectedPartition) {
        return new CustomTypeSafeMatcher<String>(String.format("String to be in partition starting at %s and ending at %s", expectedPartition.getKey(), expectedPartition.getValue())) {
            @Override
            protected boolean matchesSafely(String item) {
                BigInteger hash = new BigInteger(1, md5of(item));
                BigInteger startHash = expectedPartition.getKey();
                BigInteger endHash = expectedPartition.getValue();

                return hash.compareTo(startHash) >= 0 && hash.compareTo(endHash) <= 0;
            }

            private byte[] md5of(String string) {
                try {
                    return MessageDigest.getInstance("MD5").digest(string.getBytes());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }
}
