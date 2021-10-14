package io.github.jsssk.bf.async;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.slot.SlotStrategy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class DefaultSlotAsyncBloomFilter implements AsyncBloomFilter {

    protected List<AsyncBloomFilter> slots;

    protected BloomFilterHashStrategy strategy;

    protected SlotStrategy slotStrategy;

    @Override
    public void clear(BiConsumer<Void, Throwable> callback) {
        delete(callback);
    }

    @Override
    public void delete(BiConsumer<Void, Throwable> callback) {
        CountDownLatch latch = new CountDownLatch(slotStrategy.slotNum());
        for (AsyncBloomFilter slot : slots) {
            slot.clear((v, t) -> {
                if (t != null) {
                    callback.accept(null, t);
                } else {
                    latch.countDown();
                }
            });
        }
        CompletableFuture.runAsync(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                callback.accept(null, e);
            }
            callback.accept(null,null);
        });
    }

    @Override
    public double fpp() {
        return slotStrategy.param().getFalsePositiveProbability();
    }

    @Override
    public void put(byte[] raw, BiConsumer<Void, Throwable> callback) {
        int slot = slotStrategy.slot(raw);
        slots.get(slot).put(raw, callback);
    }


    @Override
    public void contains(byte[] raw, BiConsumer<Boolean, Throwable> callback) {
        int slot = slotStrategy.slot(raw);
        slots.get(slot).contains(raw, callback);
    }
}
