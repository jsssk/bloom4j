package io.github.jsssk.bf.slot;

import io.github.jsssk.bf.sync.BloomFilter;
import io.github.jsssk.bf.sync.TransferableBloomFilter;

import java.util.List;

public interface TransferableSlotBloomFilter extends BloomFilter {
    List<TransferableBloomFilter> getSlots();

    default void transferFrom(TransferableSlotBloomFilter src) {
        List<TransferableBloomFilter> destSlots = getSlots();
        List<TransferableBloomFilter> srcSlots  = src.getSlots();
        if (destSlots.size() != srcSlots.size()) {
            throw new RuntimeException("could not transfer.");
        }
        for (int i = 0; i < destSlots.size(); i++) {
            destSlots.get(i).transferFrom(srcSlots.get(i));
        }
    }
}
