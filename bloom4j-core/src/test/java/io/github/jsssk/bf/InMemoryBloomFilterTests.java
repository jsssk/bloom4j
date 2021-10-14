package io.github.jsssk.bf;

import io.github.jsssk.bf.slot.DefaultSlotStrategy;
import io.github.jsssk.bf.slot.SlotStrategy;
import io.github.jsssk.bf.slot.TransferableSlotBloomFilter;
import io.github.jsssk.bf.sync.DefaultBloomFilter;
import io.github.jsssk.bf.sync.DefaultSlotBloomFilter;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class InMemoryBloomFilterTests {


    long numOfItems = 100_000_000;
    double fpp = 1e-4;
    long slotSize = 1 << 23; // 1 KB
    SlotStrategy slotStrategy = DefaultSlotStrategy.of(numOfItems, fpp, slotSize);
    BloomFilterHashStrategy strategy = BloomFilterHashStrategies.MURMUR128_MITZ_64;

    TransferableSlotBloomFilter bf1;
    TransferableSlotBloomFilter bf2;

    @Before
    public void before() {
        bf1 = DefaultSlotBloomFilter.create(slotStrategy, strategy,
            (i, p) -> DefaultBloomFilter.create(strategy, p));
        bf2 = DefaultSlotBloomFilter.create(slotStrategy, strategy,
            (i, p) -> DefaultBloomFilter.create(strategy, p));
    }

    private byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void put1AndContains1() {
        bf1.put(toBytes("1"));
        assert bf1.contains(toBytes("1"));
        assert ! bf1.contains(toBytes("123"));
    }

    @Test
    public void put2AndContains2() {
        bf1.put(toBytes("2"));
        assert bf1.contains(toBytes("2"));
    }

    @Test
    public void transferBf1ToBf2() {
        bf1.put(toBytes("a"));
        bf1.put(toBytes("b"));
        bf1.put(toBytes("c"));

        assert bf1.contains(toBytes("a"));
        assert bf1.contains(toBytes("b"));
        assert bf1.contains(toBytes("c"));
        assert ! bf1.contains(toBytes("d"));

        bf2.transferFrom(bf1);

        assert bf2.contains(toBytes("a"));
        assert bf2.contains(toBytes("b"));
        assert bf2.contains(toBytes("c"));
        assert !bf2.contains(toBytes("d"));

    }
}
