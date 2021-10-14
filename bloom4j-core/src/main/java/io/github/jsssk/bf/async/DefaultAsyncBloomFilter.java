package io.github.jsssk.bf.async;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;

import java.util.function.BiConsumer;

public class DefaultAsyncBloomFilter implements AsyncBloomFilter {


    protected BloomFilterHashStrategy strategy;
    protected BloomFilterParam param;
    protected AsyncBitMap bits;

    @Override
    public void clear(BiConsumer<Void, Throwable> callback) {
        bits.clear(callback);
    }

    @Override
    public void delete(BiConsumer<Void, Throwable> callback) {
        bits.delete(callback);
    }

    @Override
    public double fpp() {
        return param.getFalsePositiveProbability();
    }

    @Override
    public void put(byte[] raw, BiConsumer<Void, Throwable> callback) {
        bits.set(strategy.indexes(raw, param), callback);
    }

    @Override
    public void contains(byte[] raw, BiConsumer<Boolean, Throwable> callback) {
        bits.allMatch(strategy.indexes(raw, param), callback);
    }
}
