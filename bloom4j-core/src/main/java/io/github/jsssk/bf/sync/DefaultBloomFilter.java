package io.github.jsssk.bf.sync;

import io.github.jsssk.bf.BloomFilterHashStrategies;
import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;
import io.github.jsssk.bf.sync.mem.InMemoryBitMap;

import java.io.Serializable;
import java.util.Iterator;

public class DefaultBloomFilter implements BloomFilter, TransferableBloomFilter, Serializable {

    /**
     * strategies of generating the k * log(M) bits required for an element
     * to be mapped to a BloomFilter of M bits and k hash functions.
     */
    protected BloomFilterHashStrategy strategy;
    /**
     * param of bloom filter
     */
    protected BloomFilterParam param;

    /**
     * lettuce redis client
     */
    protected BitMap bits;

    protected DefaultBloomFilter() {
    }
    public static DefaultBloomFilter create(BloomFilterParam param) {
        return create(BloomFilterHashStrategies.DEFAULT, param);
    }

    public static DefaultBloomFilter create(BloomFilterHashStrategy strategy, BloomFilterParam param) {
        BitMap bits = new InMemoryBitMap(param.getBitSize());
        return new DefaultBloomFilter(bits, strategy, param);
    }
    public static DefaultBloomFilter create(BitMap bits, BloomFilterParam param) {
        return create(bits, BloomFilterHashStrategies.DEFAULT, param);
    }

    public static DefaultBloomFilter create(BitMap bits, BloomFilterHashStrategy strategy, BloomFilterParam param) {
        if (bits.size() != param.getBitSize()) {
            throw new IllegalArgumentException("bits and param not match (size not match).");
        }
        return new DefaultBloomFilter(bits, strategy, param);
    }

    DefaultBloomFilter(BitMap bits, BloomFilterHashStrategy strategy, BloomFilterParam param) {
        this.bits = bits;
        this.strategy = strategy;
        this.param = param;
    }

    @Override
    public void clear() {
        bits.clear();
    }

    @Override
    public void delete() {
        bits.delete();
    }

    @Override
    public double fpp() {
        return param.getFalsePositiveProbability();
    }

    @Override
    public void put(byte[] raw) {
        bits.set(strategy.indexes(raw, param));
    }

    @Override
    public boolean contains(byte[] raw) {
        return bits.allMatch(strategy.indexes(raw, param));
    }

    @Override
    public void putBytes(long i, byte[] b) {
        bits.write(i, b);
    }

    @Override
    public void mergeBytes(long i, byte[] b) {
        bits.merge(i, b);
    }

    @Override
    public Iterator<byte[]> iterator() {
        return bits.iterator();
    }
}
