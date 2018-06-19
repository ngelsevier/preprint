package com.ssrn.authors.postgres;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Md5BasedPartition {
    private final int partitionIndex;
    private final BigInteger lowerBound;
    private final BigInteger upperBound;

    Md5BasedPartition(int partitionIndex, BigInteger lowerBound, BigInteger upperBound) {
        this.partitionIndex = partitionIndex;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    boolean contains(String string) {
        BigInteger md5Hash = new BigInteger(1, createMd5MessageDigest().digest(string.getBytes()));
        return md5Hash.compareTo(lowerBound) >= 0 && md5Hash.compareTo(upperBound) <= 0;
    }

    int getPartitionIndex() {
        return partitionIndex;
    }

    private static MessageDigest createMd5MessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
