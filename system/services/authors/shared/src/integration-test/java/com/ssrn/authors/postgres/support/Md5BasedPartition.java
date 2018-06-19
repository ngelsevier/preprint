package com.ssrn.authors.postgres.support;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.IntStream;

public class Md5BasedPartition {
    private static final BigInteger MD5_UPPERBOUND = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE);
    private final BigInteger lowerBound;
    private final BigInteger upperBound;
    private int initialId = 0;

    public static Md5BasedPartition md5BasedPartition(int partitionIndex, int totalPartitions) {
        return new Md5BasedPartition(partitionIndex, totalPartitions);
    }

    private Md5BasedPartition(int partitionIndex, int totalPartitions) {
        BigInteger partitionWidth = MD5_UPPERBOUND.add(BigInteger.ONE).divide(BigInteger.valueOf(totalPartitions));
        lowerBound = partitionWidth.multiply(BigInteger.valueOf(partitionIndex));
        upperBound = partitionWidth.multiply(BigInteger.valueOf(partitionIndex + 1)).subtract(BigInteger.ONE);
    }

    public String getRandomStringInPartition() {
        int nextId = IntStream.iterate(initialId, i -> i + 1)
                .filter(i -> isInPartition(Integer.toString(i)))
                .findFirst()
                .getAsInt();

        initialId = nextId + 1;

        return Integer.toString(nextId);
    }

    private boolean isInPartition(String i) {
        BigInteger hash = new BigInteger(1, md5of(i));
        return hash.compareTo(lowerBound) >= 0 && hash.compareTo(upperBound) <= 0;
    }

    private static byte[] md5of(String string) {
        try {
            return MessageDigest.getInstance("MD5").digest(string.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
