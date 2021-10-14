package io.github.jsssk.bf;

public interface BloomFilterHashStrategy {
    /**
     * get indexes of the certain bytes according to Hash Strategy.
     */
    long[] indexes(byte[] raw, BloomFilterParam param);
}
