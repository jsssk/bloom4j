package io.github.jsssk.bf.async;

import io.github.jsssk.bf.BloomFilterHashStrategies;
import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.async.redis.RedisAsyncBloomFilter;
import io.github.jsssk.bf.async.redis.RedisSlotAsyncBloomFilter;
import io.github.jsssk.bf.slot.DefaultSlotStrategy;
import io.github.jsssk.bf.slot.SlotStrategy;
import io.github.jsssk.bf.sync.BloomFilter;
import io.github.jsssk.bf.sync.redis.RedisSlotBloomFilter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class RedisAsyncBloomFilterTests {

    String prefix = "redis-bloom-filter-test";
    long numOfItems = 1_000_000;
    double fpp = 1e-4;
    long slotSize = 1 << 23; // 1 KB
    SlotStrategy slotStrategy = DefaultSlotStrategy.of(numOfItems, fpp, slotSize);
    BloomFilterHashStrategy strategy = BloomFilterHashStrategies.MURMUR128_MITZ_64;


    AsyncBloomFilter bf1;
    AsyncBloomFilter bf2;

    BloomFilter syncBF;
    AsyncBloomFilter asyncBF;

    @Before
    public void before() {
        RedisClient client = RedisClient.create("redis://localhost");
        StatefulRedisConnection<String, String> conn =  client.connect();

        bf1 = RedisSlotAsyncBloomFilter.create(conn.async(), prefix + 1, slotStrategy, strategy);
        bf2 = RedisSlotAsyncBloomFilter.create(conn.async(), prefix + 2, slotStrategy, strategy);

        String newPrefix = prefix + "-new";
        asyncBF = RedisSlotAsyncBloomFilter.create(conn.async(), newPrefix, slotStrategy, strategy);
        syncBF = RedisSlotBloomFilter.create(conn.sync(), newPrefix, slotStrategy, strategy);
    }

    private byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void put1AndContains1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        bf1.put(toBytes("1"), (v, t) -> latch.countDown());
        bf1.contains(toBytes("1"), (v, t) -> {
            assert v;
            latch.countDown();
        });
        bf1.contains(toBytes("2"), (v, t) -> {
            assert !v;
            latch.countDown();
        });
        latch.await();

    }


    @Test
    public void transfer1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        asyncBF.put(toBytes("1"), (v, t) -> latch.countDown());
        asyncBF.put(toBytes("2"), (v, t) -> latch.countDown());
        asyncBF.put(toBytes("3"), (v, t) -> latch.countDown());
        latch.await();

        assert syncBF.contains(toBytes("1"));
        assert syncBF.contains(toBytes("2"));
        assert syncBF.contains(toBytes("3"));

    }

    @Test
    public void transfer2() throws InterruptedException {
        syncBF.put(toBytes("1"));
        syncBF.put(toBytes("2"));
        syncBF.put(toBytes("3"));

        CountDownLatch latch = new CountDownLatch(3);
        asyncBF.contains(toBytes("1"), (v, t) -> {
            assert v;
            latch.countDown();
        });
        asyncBF.contains(toBytes("2"), (v, t) -> {
            assert v;
            latch.countDown();
        });
        asyncBF.contains(toBytes("3"), (v, t) -> {
            assert v;
            latch.countDown();
        });
        latch.await();

    }


}
