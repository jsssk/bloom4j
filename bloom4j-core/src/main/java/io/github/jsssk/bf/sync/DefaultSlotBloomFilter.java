package io.github.jsssk.bf.sync;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;
import io.github.jsssk.bf.slot.SlotStrategy;
import io.github.jsssk.bf.slot.Slots;
import io.github.jsssk.bf.slot.TransferableSlotBloomFilter;
import io.github.jsssk.bf.sync.file.FileBitMap;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DefaultSlotBloomFilter implements TransferableSlotBloomFilter, Serializable {

    public static DefaultSlotBloomFilter createFileBloomFilter(SlotStrategy slotStrategy, BloomFilterHashStrategy strategy) {
        return DefaultSlotBloomFilter.create(slotStrategy, strategy,
            (i, p) -> {
                try {
                    Path file = Files.createTempFile("slots-bloom-filter", "slot");
                    return DefaultBloomFilter.create(new FileBitMap(file, p.getBitSize()), strategy, p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public static DefaultSlotBloomFilter createFileBloomFilter(SlotStrategy slotStrategy, BloomFilterHashStrategy strategy, String path, String prefix) {
        return DefaultSlotBloomFilter.create(slotStrategy, strategy,
            (i, p) -> DefaultBloomFilter.create(new FileBitMap(Paths.get(Slots.identifier(path + "/" + prefix, i)), p.getBitSize()), strategy, p));
    }

    public static DefaultSlotBloomFilter createInMemoryBloomFilter(SlotStrategy slotStrategy, BloomFilterHashStrategy strategy) {
        return create(slotStrategy, strategy);
    }

    public static DefaultSlotBloomFilter create(SlotStrategy slotStrategy, BloomFilterHashStrategy strategy) {
        return new DefaultSlotBloomFilter(slotStrategy, strategy, (i, p) -> DefaultBloomFilter.create(strategy, p));
    }

    public static DefaultSlotBloomFilter create(SlotStrategy slotStrategy, BloomFilterHashStrategy strategy,
                                                BiFunction<Integer, BloomFilterParam, TransferableBloomFilter> bloomFilterSupplier) {
        return new DefaultSlotBloomFilter(slotStrategy, strategy, bloomFilterSupplier);
    }

    protected DefaultSlotBloomFilter() {}


    private DefaultSlotBloomFilter(SlotStrategy slotStrategy, BloomFilterHashStrategy strategy,
                                   BiFunction<Integer, BloomFilterParam, TransferableBloomFilter> bloomFilterSupplier) {
        this.slotStrategy = slotStrategy;
        this.strategy = strategy;

        this.slots = new ArrayList<>(slotStrategy.slotNum());
        for (int i = 0; i < slotStrategy.slotNum(); i++) {
            BloomFilterParam slotParam = BloomFilterParam.of(
                slotStrategy.param().getNumOfItems() / slotStrategy.slotNum(),
                slotStrategy.param().getFalsePositiveProbability());
            this.slots.add(bloomFilterSupplier.apply(i, slotParam));
        }
    }


    protected List<TransferableBloomFilter> slots;

    protected BloomFilterHashStrategy strategy;

    protected SlotStrategy slotStrategy;


    @Override
    public void clear() {
        for (BloomFilter slot : slots) {
            slot.clear();
        }
    }

    @Override
    public void delete() {
        for (BloomFilter slot : slots) {
            slot.delete();
        }
    }

    @Override
    public double fpp() {
        return slotStrategy.param().getFalsePositiveProbability();
    }

    @Override
    public void put(byte[] raw) {
        slots.get(slotStrategy.slot(raw)).put(raw);
    }

    @Override
    public boolean contains(byte[] raw) {
        return slots.get(slotStrategy.slot(raw)).contains(raw);
    }

    @Override
    public List<TransferableBloomFilter> getSlots() {
        return slots;
    }
}
