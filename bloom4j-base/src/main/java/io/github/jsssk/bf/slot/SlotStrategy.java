package io.github.jsssk.bf.slot;

import io.github.jsssk.bf.BloomFilterParam;

public interface SlotStrategy {

    long slotSize();

    int slotNum();

    int slot(byte[] raw);

    BloomFilterParam param();
}
